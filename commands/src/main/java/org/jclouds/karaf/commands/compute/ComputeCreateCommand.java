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

@Command(scope = "jclouds", name = "compute-service-create", description = "Creates a compute service")
public class ComputeCreateCommand extends ComputeCommandSupport {

    private static final String FACTORY_FILTER = "(service.factoryPid=%s)";

    @Option(name = "--add-option", multiValued = true, description = "Adds a key value pair to the configuration.")
    protected String[] options;

    @Option(name = "--no-wait", multiValued = true, description = "Don't wait for compute service registration.")
    protected boolean noWait;

    private BundleContext bundleContext;
    private ConfigurationAdmin configAdmin;

    @Override
    protected Object doExecute() throws Exception {
        if (provider == null && api == null) {
            System.err.println("You need to specify at least a valid provider or api.");
            return null;
        }

        Map<String, String> props = parseOptions(options);
        registerComputeService(configAdmin, provider, api, identity, credential, props);
        if (!noWait) {
            waitForComputeService(bundleContext, provider, api);
        }
        return null;
    }

    /**
     * Creates a {@link Map} from the specified key / value options specified.
     *
     * @param options
     * @return
     */
    private Map<String, String> parseOptions(String[] options) {
        Map<String, String> props = new HashMap<String, String>();
        if (options != null && options.length > 1) {
            for (String option : options) {
                if (option.contains("=")) {
                    String key = option.substring(0, option.indexOf("="));
                    String value = option.substring(option.lastIndexOf("=") + 1);
                    props.put(key, value);
                }
            }
        }
        return props;
    }

    /**
     * Registers a {@link ComputeService}
     *
     * @param configurationAdmin
     * @param provider
     * @param api
     * @param identity
     * @param credential
     * @param props
     * @throws Exception
     */
    private void registerComputeService(final ConfigurationAdmin configurationAdmin, final String provider, final String api, final String identity, final String credential, final Map<String, String> props) throws Exception {
        Runnable registrationTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Configuration configuration = findOrCreateFactoryConfiguration(configurationAdmin, "org.jclouds.compute", provider, api);
                    if (configuration != null) {
                        Dictionary dictionary = configuration.getProperties();
                        if (dictionary == null) {
                            dictionary = new Properties();
                        }

                        if (provider != null) {
                            dictionary.put("provider", provider);
                        }
                        if (api != null) {
                            dictionary.put("api", api);
                        }
                        dictionary.put("credential", credential);
                        dictionary.put("identity", identity);
                        for (Map.Entry<String, String> entry : props.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            dictionary.put(key, value);
                        }
                        configuration.update(dictionary);
                    }
                } catch (Exception ex) {
                    //noop
                }
            }
        };
        new Thread(registrationTask).start();
    }

    /**
     * Finds a {@link Configuration} if exists, or creates a new one.
     *
     * @param configurationAdmin
     * @param factoryPid
     * @param provider
     * @param api
     * @return
     * @throws IOException
     */
    private Configuration findOrCreateFactoryConfiguration(ConfigurationAdmin configurationAdmin, String factoryPid, String provider, String api) throws IOException {
        Configuration configuration = null;
        if (configurationAdmin != null) {
            try {
                Configuration[] configurations = configurationAdmin.listConfigurations(String.format(FACTORY_FILTER, factoryPid));
                if (configurations != null) {
                    for (Configuration conf : configurations) {
                        Dictionary dictionary = conf.getProperties();
                        if (dictionary != null && provider != null && provider.equals(dictionary.get("provider"))) {
                            return conf;
                        } else if (dictionary != null && api != null && api.equals(dictionary.get("api"))) {
                            return conf;
                        }
                    }
                }
            } catch (Exception e) {
                //noop
            }
            configuration = configurationAdmin.createFactoryConfiguration(factoryPid, null);
        }
        return configuration;
    }

    /**
     * Waits for the {@link ComputeService} registration.
     *
     * @param bundleContext
     * @param provider
     * @param api
     * @return
     */
    public synchronized ComputeService waitForComputeService(BundleContext bundleContext, String provider, String api) {
        ComputeService computeService = null;
        try {
            for (int r = 0; r < 6; r++) {
                ServiceReference[] references = null;
                if (provider != null) {
                    references = bundleContext.getAllServiceReferences(ComputeService.class.getName(), "(provider=" + provider + ")");
                } else if (api != null) {
                    references = bundleContext.getAllServiceReferences(ComputeService.class.getName(), "(api=" + api + ")");
                }
                if (references != null && references.length > 0) {
                    computeService = (ComputeService) bundleContext.getService(references[0]);
                    return computeService;
                }
                Thread.sleep(10000L);
            }
        } catch (Exception e) {
            //noop
        }
        return computeService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public ConfigurationAdmin getConfigAdmin() {
        return configAdmin;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }
}
