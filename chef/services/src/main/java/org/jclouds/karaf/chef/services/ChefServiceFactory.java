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

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.ApiPredicates;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.ChefService;
import org.jclouds.karaf.chef.core.ChefConstants;
import org.jclouds.karaf.chef.core.ChefHelper;
import org.jclouds.karaf.services.ServiceFactorySupport;
import org.jclouds.karaf.services.InvalidConfigurationException;
import org.jclouds.providers.ProviderMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;



public class ChefServiceFactory extends ServiceFactorySupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChefServiceFactory.class);
    private final BundleContext bundleContext;


    public ChefServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getName() {
        return "Chef Service Factory";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        ServiceRegistration newRegistration = null;
        try {
            if (properties != null) {
                Properties props = new Properties();
                for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
                    Object key = e.nextElement();
                    Object val = properties.get(key);
                    props.put(key, val);
                }

                String api = (String) properties.get(ChefConstants.API);

                ProviderMetadata providerMetadata = null;
                ApiMetadata apiMetadata = null;
                if (!Strings.isNullOrEmpty(api) && installedApis.containsKey(api)) {
                    apiMetadata = installedApis.get(api);
                    validate(apiMetadata, properties);
                } else {

                    if (!Strings.isNullOrEmpty(api)) {
                        apiPids.put(api, pid);
                    }
                    pendingPids.put(pid, properties);
                    LOGGER.warn("Api {} is not currently installed. Service will resume once the the api is installed.", api);
                    return;
                }

                String id = (String) properties.get(ChefConstants.NAME);
                String clientName = (String) properties.get(ChefConstants.CLIENT_NAME);
                String clientKeyFile = (String) properties.get(ChefConstants.CLIENT_KEY_FILE);
                String clientCredential = (String) properties.get(ChefConstants.CLIENT_CREDENTIAL);
                String validatorName = (String) properties.get(ChefConstants.VALIDATOR_NAME);
                String validatorKeyFile = (String) properties.get(ChefConstants.VALDIATOR_KEY_FILE);
                String validatorCredential = (String) properties.get(ChefConstants.VALDIATOR_CREDENTIAL);
                String endpoint = (String) properties.get(ChefConstants.ENDPOINT);

                ChefService service = ChefHelper.createChefService(apiMetadata, id, clientName, clientCredential, clientKeyFile, validatorName, validatorCredential, validatorKeyFile, endpoint);
                newRegistration = bundleContext.registerService(
                        ChefService.class.getName(), service, properties);

                //If all goes well remove the pending pid.
                if (pendingPids.containsKey(pid)) {
                    activePids.put(pid, pendingPids.remove(pid));
                }
            }
        } catch (InvalidConfigurationException ex) {
            LOGGER.warn("Invalid configuration: {}", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Error creating compute service.", ex);
        } finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                oldRegistration.unregister();
            }
        }
    }

    /**
     * Checks if configuration is valid for the specified {@link ApiMetadata}
     *
     * @param apiMetadata
     * @param properties
     * @throws InvalidConfigurationException
     */
    public static void validate(ApiMetadata apiMetadata, Dictionary properties) throws InvalidConfigurationException {
        if (Strings.isNullOrEmpty((String) properties.get(ChefConstants.CLIENT_NAME))) {
            throw new InvalidConfigurationException("No client name specified.");
        }

        if (Strings.isNullOrEmpty((String) properties.get(ChefConstants.VALIDATOR_NAME))) {
            throw new InvalidConfigurationException("No validator name specified.");
        }

        if (Strings.isNullOrEmpty((String) properties.get(ChefConstants.ENDPOINT)) && !apiMetadata.getDefaultEndpoint().isPresent()) {
            throw new InvalidConfigurationException("No endpoint specified specified.");
        }
    }


    @Override
    public boolean apply(ProviderMetadata provider) {
        return false;
    }

    @Override
    public boolean apply(ApiMetadata api) {
        return ApiPredicates.contextAssignableFrom(TypeToken.of(ChefContext.class)).apply(api);
    }
}