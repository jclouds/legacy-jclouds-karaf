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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.ApiPredicates;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.services.InvalidConfigurationException;
import org.jclouds.karaf.services.ServiceFactorySupport;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.management.config.ManagementLifecycle;
import org.jclouds.management.internal.BaseManagementContext;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.ProviderPredicates;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

public class BlobStoreServiceFactory extends ServiceFactorySupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreServiceFactory.class);

    private final BundleContext bundleContext;

    public BlobStoreServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getName() {
        return "BlobStore Service Factory";
    }

    public synchronized void updated(String pid, Dictionary properties) throws ConfigurationException {
        ServiceRegistration newRegistration = null;
        try {
            if (properties != null) {
                Properties props = new Properties();
                for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
                    Object key = e.nextElement();
                    Object val = properties.get(key);
                    props.put(key, val);
                }
                String provider = (String) properties.get(Constants.PROVIDER);
                String api = (String) properties.get(Constants.API);

                ProviderMetadata providerMetadata = null;
                ApiMetadata apiMetadata = null;


                if (!Strings.isNullOrEmpty(provider) && installedProviders.containsKey(provider)) {
                    providerMetadata = installedProviders.get(provider);
                    validate(providerMetadata, properties);
                } else if (!Strings.isNullOrEmpty(api) && installedApis.containsKey(api)) {
                    apiMetadata = installedApis.get(api);
                    validate(apiMetadata, properties);
                } else {
                    if (!Strings.isNullOrEmpty(provider)) {
                        providerPids.put(provider, pid);
                    }
                    if (!Strings.isNullOrEmpty(api)) {
                        apiPids.put(api, pid);
                    }
                    pendingPids.put(pid, properties);
                    LOGGER.warn("Provider {} or Api {} is not currently installed. Service will resume once the the provider is installed.", provider, api);
                    return;
                }


                //We are removing credentials as we don't want them to be visible in the service registration.
                String id = (String) properties.get(Constants.NAME);
                String identity = (String) properties.remove(Constants.IDENTITY);
                String credential = (String) properties.remove(Constants.CREDENTIAL);
                String endpoint = (String) properties.get(Constants.ENDPOINT);

                BlobStoreContext context = null;
                ContextBuilder builder = null;
                if (providerMetadata != null) {
                    builder = ContextBuilder.newBuilder(providerMetadata);
                } else if (apiMetadata != null) {
                    builder = ContextBuilder.newBuilder(apiMetadata);
                } else {
                    return;
                }

                if (!Strings.isNullOrEmpty(endpoint)) {
                    builder = builder.endpoint(endpoint);
                }
                context = builder.name(id).credentials(identity, credential)
                        .modules(ImmutableSet.<Module>of(new Log4JLoggingModule(), new ManagementLifecycle(BaseManagementContext.INSTANCE)))
                        .overrides(props)
                        .build(BlobStoreContext.class);

                BlobStore blobStore = context.getBlobStore();
                newRegistration = bundleContext.registerService(
                        BlobStore.class.getName(), blobStore, properties);

                //If all goes well move the pending pid to the active pids.
                if (pendingPids.containsKey(pid)) {
                    activePids.put(pid, pendingPids.remove(pid));
                }
            }
        } catch (InvalidConfigurationException ex) {
            LOGGER.warn("Invalid configuration: {}", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Error creating blobstore service.", ex);
        } finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                System.out.println("Unregistering BlobStore " + pid);
                oldRegistration.unregister();
            }
        }
    }

    @Override
    public boolean apply(ProviderMetadata provider) {
        return ProviderPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(provider);
    }

    @Override
    public boolean apply(ApiMetadata api) {
        return ApiPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(api);
    }
}