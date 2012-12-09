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

package org.jclouds.karaf.chef.services;

import org.jclouds.osgi.ApiListener;
import org.jclouds.osgi.ProviderListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.Hashtable;

public class Activator implements BundleActivator {

    ServiceRegistration chefFactoryRegistration;
    ChefServiceFactory chefServiceFactory;

    public void start(BundleContext context) throws Exception {
        registerChefServiceFactory(context);
    }

    public void stop(BundleContext context) throws Exception {
        if (chefFactoryRegistration != null) {
            chefFactoryRegistration.unregister();
        }
    }

    /**
     * Registers a {@link ManagedServiceFactory} for the jclouds compute.
     *
     * @param context
     */
    private void registerChefServiceFactory(BundleContext context) {

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "org.jclouds.chef");
        chefServiceFactory = new ChefServiceFactory(context);
        chefFactoryRegistration = context.registerService(new String[]{ManagedServiceFactory.class.getName(), ProviderListener.class.getName(), ApiListener.class.getName()},
                chefServiceFactory, properties);
    }
}
