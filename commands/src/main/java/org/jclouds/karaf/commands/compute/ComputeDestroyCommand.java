/*
 * Copyright (C) 2011, the original authors
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.karaf.commands.compute;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.apis.Apis;
import org.jclouds.compute.ComputeService;
import org.jclouds.providers.Providers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Command(scope = "jclouds", name = "compute-service-destroy", description = "Destroys a compute service")
public class ComputeDestroyCommand extends ComputeServiceCommand {

    private BundleContext bundleContext;

    @Override
    protected Object doExecute() throws Exception {
        if (provider == null && api == null) {
            System.err.println("You need to specify at least a valid provider or api.");
            return null;
        }

        Configuration configuration = findOrCreateFactoryConfiguration(configAdmin, "org.jclouds.compute", provider, api);
        if (configuration != null) {
            configuration.delete();
        } else {
            System.out.println("No service found for provider / api");
        }
        return null;
    }





    public ConfigurationAdmin getConfigAdmin() {
        return configAdmin;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }
}
