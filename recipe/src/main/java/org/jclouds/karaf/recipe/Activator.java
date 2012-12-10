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

package org.jclouds.karaf.recipe;

import org.jclouds.karaf.recipe.RecipeManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private RecipeManager recipeManager = new RecipeManagerImpl();
    private ServiceTracker recipeProviderTracker;
    private ServiceRegistration recipeManagerRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        recipeProviderTracker = new ServiceTracker(context, RecipeProvider.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object obj = super.addingService(reference);
                if (RecipeProvider.class.isAssignableFrom(obj.getClass())) {
                    recipeManager.bind((RecipeProvider) obj);
                }
                return obj;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                if (RecipeProvider.class.isAssignableFrom(service.getClass())) {
                    recipeManager.unibind((RecipeProvider) service);
                }
                super.removedService(reference, service);
            }
        };
        recipeProviderTracker.open();
        recipeManagerRegistration = context.registerService(RecipeManager.class.getName(), recipeManager, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (recipeProviderTracker != null) {
            recipeProviderTracker.close();
        }
        if (recipeManagerRegistration != null) {
            recipeManagerRegistration.unregister();
        }
    }
}
