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

package org.jclouds.karaf.services.internal;

import java.util.Hashtable;
import org.jclouds.osgi.ApiListener;
import org.jclouds.osgi.ProviderListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public class Activator implements BundleActivator {

    ServiceRegistration computeFactoryRegistration;
    ServiceRegistration blobStoreFactoryRegistration;
    ComputeServiceFactory computeFactory;
    BlobStoreServiceFactory blobStoreFactory;

    public void start(BundleContext context) throws Exception {
        registerComputeServiceFactory(context);
        registerBlobstoreServiceFactory(context);
    }

    public void stop(BundleContext context) throws Exception {
        if (computeFactoryRegistration != null) {
            computeFactoryRegistration.unregister();
        }
        if (blobStoreFactoryRegistration != null) {
            blobStoreFactoryRegistration.unregister();
        }
    }

    /**
     * Registers a {@link ManagedServiceFactory} for the jclouds compute.
     *
     * @param context
     */
    private void registerComputeServiceFactory(BundleContext context) {

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "org.jclouds.compute");
        computeFactory = new ComputeServiceFactory(context);
        computeFactoryRegistration = context.registerService(new String[]{ManagedServiceFactory.class.getName(), ProviderListener.class.getName(), ApiListener.class.getName()},
                computeFactory, properties);
    }

    /**
     * Registers a {@link ManagedServiceFactory} for the jclouds blobstore.
     *
     * @param context
     */
    private void registerBlobstoreServiceFactory(BundleContext context) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "org.jclouds.blobstore");
        blobStoreFactory = new BlobStoreServiceFactory(context);
        blobStoreFactoryRegistration = context.registerService(new String[]{ManagedServiceFactory.class.getName(), ProviderListener.class.getName(), ApiListener.class.getName()},
                blobStoreFactory, properties);
    }
}
