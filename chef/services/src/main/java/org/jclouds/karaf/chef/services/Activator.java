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

import com.google.common.collect.Maps;
import org.jclouds.chef.ChefService;
import org.jclouds.karaf.recipe.RecipeProvider;
import org.jclouds.osgi.ApiListener;
import org.jclouds.osgi.ProviderListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Hashtable;
import java.util.Map;

public class Activator implements BundleActivator {

    private ServiceRegistration chefFactoryRegistration;
    private ChefServiceFactory chefServiceFactory;
    private ServiceTracker chefServiceTracker;

    private Map<String, ServiceRegistration> registrationMap = Maps.newConcurrentMap();

    public void start(final BundleContext context) throws Exception {
        registerChefServiceFactory(context);
        //We use the system bundle context to avoid issues with InvlidBundleContext.
        chefServiceTracker = new ServiceTracker(context.getBundle(0).getBundleContext(), ChefService.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object obj =  super.addingService(reference);
                if (ChefService.class.isAssignableFrom(obj.getClass())) {
                    String serviceId =  String.valueOf(reference.getProperty(Constants.SERVICE_ID));
                    registerRecipeProviderForService(context, serviceId, (ChefService) obj);
                }
                return obj;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                String serviceId =  String.valueOf(reference.getProperty(Constants.SERVICE_ID));
                unregisterRecipeProviderForService(context, serviceId, (ChefService) service);
                super.removedService(reference, service);
            }
        };
        chefServiceTracker.open();
    }

    public void stop(BundleContext context) throws Exception {
        if (chefFactoryRegistration != null) {
            chefFactoryRegistration.unregister();
        }
        if (chefServiceTracker != null) {
            chefServiceTracker.close();
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

    private void registerRecipeProviderForService(BundleContext context, String serviceId, ChefService chefService) {
        ChefRecipeProvider chefRecipeProvider = new ChefRecipeProvider(chefService);
        ServiceRegistration registration = context.registerService(RecipeProvider.class.getName(), chefRecipeProvider, null);
        registrationMap.put(serviceId, registration);
    }

    private void unregisterRecipeProviderForService(BundleContext context, String serviceId, ChefService chefService) {
        if (registrationMap.containsKey(serviceId)) {
            ServiceRegistration registration = registrationMap.remove(serviceId);
            try {
                registration.unregister();
            } catch (Exception ex) {
                //ignore
            }
        }
    }
}
