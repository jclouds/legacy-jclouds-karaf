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

package org.jclouds.karaf.cache.utils;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.CacheManager;
import org.jclouds.karaf.cache.Cacheable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class CacheUtils {

    private CacheUtils() {
        //Utility Class
    }

    /**
     * Creates a {@link ServiceTracker} that upon Service Registration, it binds the Service to the {@link CacheManager}.
     * @param context
     * @param serviceClass
     * @param cacheManager
     * @param <T>
     * @return
     */
    public static <T> ServiceTracker createServiceCacheTracker(final BundleContext context, final Class<T> serviceClass, final CacheManager<T> cacheManager) {
        return new ServiceTracker(context, ComputeService.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                if (serviceClass.isAssignableFrom(service.getClass())) {
                    cacheManager.bindService((T) service);
                }
                return service;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (serviceClass.isAssignableFrom(service.getClass())) {
                    cacheManager.unbindService((T) service);
                }
                super.removedService(reference, service);
            }
        };
    }

    /**
     * Creates a {@link ServiceTracker} which binds {@link org.jclouds.karaf.cache.Cacheable} to {@link CacheManager}.
     * @param context
     * @param cacheManager
     * @return
     */
    public static <T> ServiceTracker createCacheableTracker(final BundleContext context, final String type, final CacheManager<T> cacheManager) throws InvalidSyntaxException {
        return new ServiceTracker(context, FrameworkUtil.createFilter("(&(cache-type="+type+")(objectClass=org.jclouds.karaf.cache.Cacheable))"), null) {
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
