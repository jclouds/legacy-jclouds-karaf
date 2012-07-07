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

package org.jclouds.karaf.commands.blobstore;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.providers.Providers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Command(scope = "jclouds", name = "blobstore-service-create",description = "Creates a BlobStore service.")
public class BlobStoreCreateCommand extends BlobStoreServiceCommand {

    @Option(name = "--add-option", multiValued = true, description = "Adds a key value pair to the configuration.")
    protected String[] options;

    @Option(name = "--no-wait", multiValued = true, description = "Don't wait for blob store service registration.")
    protected boolean noWait;

    private BundleContext bundleContext;

    @Override
    protected Object doExecute() throws Exception {
        if (provider == null && api == null) {
            System.err.println("You need to specify at least a valid provider or api.");
            return null;
        }

        Map<String, String> props = parseOptions(options);
        registerBlobStore(configAdmin, provider, api, identity, credential, props);
        if (noWait) {
            return null;
        } else if (!isProviderOrApiInstalled(provider, api)) {
            System.out.println("Provider / api currently not installed. Service will be created once it does get installed.");
            return null;
        } else {
            System.out.println("Waiting for blobstore  service.");
            waitForBlobStore(bundleContext, provider, api);
        }
        return null;
    }

    /**
     * Returns true if provider or api is currently installed.
     *
     * @param provider
     * @param api
     * @return
     */
    private boolean isProviderOrApiInstalled(String provider, String api) {
        boolean providerOrApiFound = false;
        try {
            Providers.withId(provider);
            providerOrApiFound = true;
        } catch (Exception ex) {
            //ignore
        }
        try {
            Apis.withId(api);
            providerOrApiFound = true;
        } catch (Exception ex) {
            //ignore
        }
        return providerOrApiFound;
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
     * Registers a {@link org.jclouds.blobstore.BlobStore}
     *
     * @param configurationAdmin
     * @param provider
     * @param api
     * @param identity
     * @param credential
     * @param props
     * @throws Exception
     */
    private void registerBlobStore(final ConfigurationAdmin configurationAdmin, final String provider, final String api, final String identity, final String credential, final Map<String, String> props) throws Exception {
        Runnable registrationTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Configuration configuration = findOrCreateFactoryConfiguration(configurationAdmin, "org.jclouds.blobstore", provider, api);
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
     * Waits for the {@link org.jclouds.blobstore.BlobStore} registration.
     *
     * @param bundleContext
     * @param provider
     * @param api
     * @return
     */
    public synchronized BlobStore waitForBlobStore(BundleContext bundleContext, String provider, String api) {
        BlobStore blobStore = null;
        try {
            for (int r = 0; r < 6; r++) {
                ServiceReference[] references = null;
                if (provider != null) {
                    references = bundleContext.getAllServiceReferences(BlobStore.class.getName(), "(provider=" + provider + ")");
                } else if (api != null) {
                    references = bundleContext.getAllServiceReferences(BlobStore.class.getName(), "(api=" + api + ")");
                }
                if (references != null && references.length > 0) {
                    blobStore = (BlobStore) bundleContext.getService(references[0]);
                    return blobStore;
                }
                Thread.sleep(10000L);
            }
        } catch (Exception e) {
            //noop
        }
        return blobStore;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
