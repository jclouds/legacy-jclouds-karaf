/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.karaf.cache;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.blobstore.BlobCacheManager;
import org.jclouds.karaf.cache.compute.ComputeCacheManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Properties;

public class Activator implements BundleActivator {

     private ServiceTracker computeServiceTracker;
     private ServiceTracker computeCacheableTracker;

     private ServiceTracker blobStoreTracker;
     private ServiceTracker blobStoreCacheableTracker;

     private ServiceRegistration cacheProviderRegistration;

     private final ComputeCacheManager computeCacheManager = new ComputeCacheManager();
     private final BlobCacheManager blobCacheManager = new BlobCacheManager();



    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this
     *                   bundle is marked as stopped and the Framework will remove this
     *                   bundle's listeners, unregister all services registered by this
     *                   bundle, and release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        CacheProvider cacheProvider = new BasicCacheProvider();
        cacheProviderRegistration = context.registerService(CacheProvider.class.getName(), cacheProvider, new Properties());

        computeServiceTracker = createComputeServiceTracker(context, computeCacheManager);
        computeCacheableTracker = createComputeCacheableTracker(context, computeCacheManager);
        blobStoreTracker = createBlobStoreServiceTracker(context, blobCacheManager);
        blobStoreCacheableTracker = createBlobStoreCacheableTracker(context, blobCacheManager);

        computeServiceTracker.open();
        computeCacheableTracker.open();
        blobStoreTracker.open();
        blobStoreCacheableTracker.open();

        computeCacheManager.init();
        blobCacheManager.init();

    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the <code>BundleActivator.start</code>
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the
     *                   bundle is still marked as stopped, and the Framework will remove
     *                   the bundle's listeners, unregister all services registered by the
     *                   bundle, and release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        computeCacheManager.destroy();
        blobCacheManager.destroy();

        if (cacheProviderRegistration != null) {
            cacheProviderRegistration.unregister();
        }
        if (computeServiceTracker != null) {
            computeServiceTracker.close();
        }
        if (computeCacheableTracker != null) {
            computeCacheableTracker.close();
        }
        if (blobStoreTracker != null) {
            blobStoreTracker.close();
        }
        if (blobStoreCacheableTracker != null) {
            blobStoreCacheableTracker.close();
        }
    }


    /**
     * Creates a {@link ServiceTracker} which binds {@link ComputeService} to {@link ComputeCacheManager}.
     * @param context
     * @param cacheManager
     * @return
     */
    private ServiceTracker createComputeServiceTracker(final BundleContext context, final ComputeCacheManager cacheManager) {
       return new ServiceTracker(context, ComputeService.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                if (ComputeService.class.isAssignableFrom(service.getClass())) {
                    cacheManager.bindService((ComputeService) service);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (ComputeService.class.isAssignableFrom(service.getClass())) {
                    cacheManager.unbindService((ComputeService) service);
                }
                super.removedService(reference, service);
            }
        };
    }



    /**
     * Creates a {@link ServiceTracker} which binds {@link Cacheable} to {@link ComputeCacheManager}.
     * @param context
     * @param cacheManager
     * @return
     */
    private ServiceTracker createComputeCacheableTracker(final BundleContext context, final ComputeCacheManager cacheManager) throws InvalidSyntaxException {
        return new ServiceTracker(context, FrameworkUtil.createFilter("(&(cache-type=jclouds.computeservice)(objectClass=org.jclouds.karaf.cache.Cacheable))"), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                if (Cacheable.class.isAssignableFrom(service.getClass())) {
                    cacheManager.bindCachable((Cacheable) service);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (Cacheable.class.isAssignableFrom(service.getClass())) {
                    cacheManager.unbindCachable((Cacheable) service);
                }
                super.removedService(reference, service);
            }
        };
    }

    /**
     * Creates a {@link ServiceTracker} which binds {@link ComputeService} to {@link ComputeCacheManager}.
     * @param context
     * @param blobCacheManager
     * @return
     */
    private ServiceTracker createBlobStoreServiceTracker(final BundleContext context, final BlobCacheManager blobCacheManager) {
        return new ServiceTracker(context, ComputeService.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                if (BlobStore.class.isAssignableFrom(service.getClass())) {
                    blobCacheManager.bindService((BlobStore) service);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (BlobStore.class.isAssignableFrom(service.getClass())) {
                    blobCacheManager.unbindService((BlobStore) service);
                }
                super.removedService(reference, service);
            }
        };
    }

    /**
     * Creates a {@link ServiceTracker} which binds {@link Cacheable} to {@link BlobCacheManager}.
     * @param context
     * @param cacheManager
     * @return
     */
    private ServiceTracker createBlobStoreCacheableTracker(final BundleContext context, final BlobCacheManager cacheManager) throws InvalidSyntaxException {
        return new ServiceTracker(context, FrameworkUtil.createFilter("(&(cache-type=jclouds.blobstore)(objectClass=org.jclouds.karaf.cache.Cacheable))"), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                if (Cacheable.class.isAssignableFrom(service.getClass())) {
                    cacheManager.bindCachable((Cacheable) service);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (Cacheable.class.isAssignableFrom(service.getClass())) {
                    cacheManager.unbindCachable((Cacheable) service);
                }
                super.removedService(reference, service);
            }
        };
    }

}
