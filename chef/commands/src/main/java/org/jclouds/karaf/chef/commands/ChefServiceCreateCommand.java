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

package org.jclouds.karaf.chef.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.apis.Apis;
import org.jclouds.chef.ChefService;
import org.jclouds.karaf.chef.core.ChefConstants;
import org.jclouds.karaf.chef.core.ChefHelper;
import org.jclouds.karaf.core.Constants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Command(scope = "chef", name = "service-create", description = "Creates a chef service")
public class ChefServiceCreateCommand extends ChefCommandWithOptions {

    @Option(name = "--add-option", multiValued = true, description = "Adds a key value pair to the configuration.")
    protected String[] options;

    @Option(name = "--no-wait", multiValued = true, description = "Don't wait for compute service registration.")
    protected boolean noWait;

    private BundleContext bundleContext;

    @Override
    protected Object doExecute() throws Exception {
        if (name == null && api != null) {
            name = api;
        }

        Map<String, String> props = parseOptions(options);
        registerChefService(configAdmin, name, api, clientName, clientKeyFile, validatorName, validatorKeyFile, endpoint, props);
        if (noWait) {
            return null;
        } else if (!isApiInstalled(api)) {
            System.out.println("Provider / api currently not installed. Service will be created once it does get installed.");
            return null;
        } else {
            System.out.println(String.format("Waiting for chef service with name: %s.", name));
            waitForChefService(bundleContext, name, api);
        }
        return null;
    }

    /**
     * Returns true if provider or api is currently installed.
     *
     * @param api
     * @return
     */
    private boolean isApiInstalled(String api) {
        boolean apiFound = false;
        try {
            Apis.withId(api);
            apiFound = true;
        } catch (Exception ex) {
            // ignore
        }
        return apiFound;
    }

    /**
     * Creates a {@link java.util.Map} from the specified key / value options specified.
     *
     * @param options
     * @return
     */
    private Map<String, String> parseOptions(String[] options) {
        Map<String, String> props = new HashMap<String, String>();
        if (options != null && options.length >= 1) {
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
     * Registers a {@link ChefService}
     *
     * @param configurationAdmin
     * @param name
     * @param api
     * @param clientName
     * @param clientKeyFile
     * @param validator
     * @param validatorKeyFile
     * @param endpoint
     * @param props
     * @throws Exception
     */
    private void registerChefService(final ConfigurationAdmin configurationAdmin, final String name,
                                     final String api, final String clientName, final String clientKeyFile, final String validator, final String validatorKeyFile, final String endpoint,
                                     final Map<String, String> props) throws Exception {
        Runnable registrationTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Configuration configuration = findOrCreateFactoryConfiguration(configurationAdmin, "org.jclouds.chef", name, api);
                    if (configuration != null) {
                        @SuppressWarnings("unchecked")
                        Dictionary<Object, Object> dictionary = configuration.getProperties();
                        if (dictionary == null) {
                            dictionary = new Properties();
                        }

                        String apiValue = ChefHelper.getChefApi(api);
                        String clientNameValue = ChefHelper.getClientName(clientName);
                        String clientKeyFileValue = ChefHelper.getClientKeyFile(clientKeyFile);
                        String validatorNameValue = ChefHelper.getValidatorName(validator);
                        String validatorKeyFileValue = ChefHelper.getValidatorKeyFile(validatorKeyFile);
                        String endpointValue = ChefHelper.getChefEndpoint(endpoint);

                        if (name != null) {
                            dictionary.put(ChefConstants.NAME, name);
                        }
                        if (apiValue != null) {
                            dictionary.put(ChefConstants.API, apiValue);
                        }
                        if (endpointValue != null) {
                            dictionary.put(ChefConstants.ENDPOINT, endpointValue);
                        }
                        if (clientNameValue != null) {
                            dictionary.put(ChefConstants.CLIENT_NAME, clientNameValue);
                        }
                        if (clientKeyFileValue != null) {
                            dictionary.put(ChefConstants.CLIENT_KEY_FILE, clientKeyFileValue);
                        }
                        if (validatorNameValue != null) {
                            dictionary.put(ChefConstants.VALIDATOR_NAME, validatorNameValue);
                        }
                        if (validatorKeyFileValue != null) {
                            dictionary.put(ChefConstants.VALIDATOR_KEY_FILE, validatorKeyFileValue);
                        }

                        for (Map.Entry<String, String> entry : props.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            dictionary.put(key, value);
                        }
                        configuration.update(dictionary);
                    }
                } catch (Exception ex) {
                    // noop
                }
            }
        };
        new Thread(registrationTask).start();
    }

    /**
     * Waits for the {@link ChefService} registration.
     *
     * @param bundleContext
     * @param api
     * @return
     */
    public synchronized ChefService waitForChefService(BundleContext bundleContext, String name, String api) {
        ChefService chefService = null;
        try {
            for (int r = 0; r < 6; r++) {
                ServiceReference[] references = null;
                if (name != null) {
                    references = bundleContext.getAllServiceReferences(ChefService.class.getName(), "(" + Constants.NAME + "="
                            + name + ")");
                } else if (api != null) {
                    references = bundleContext.getAllServiceReferences(ChefService.class.getName(), "(" + Constants.API + "=" + api + ")");
                }

                if (references != null && references.length > 0) {
                    chefService = (ChefService) bundleContext.getService(references[0]);
                    return chefService;
                }
                Thread.sleep(10000L);
            }
        } catch (Exception e) {
            // noop
        }
        return chefService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
