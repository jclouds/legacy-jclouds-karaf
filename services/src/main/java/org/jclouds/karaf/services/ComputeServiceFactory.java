/*
 * Copyright (C) FuseSource, Inc.
 *   http://fusesource.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.jclouds.karaf.services;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.karaf.core.ComputeProviderOrApiListener;
import org.jclouds.karaf.core.ComputeProviderOrApiRegistry;
import org.jclouds.karaf.core.ComputeServiceEventProxy;
import org.jclouds.karaf.core.CredentialStore;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

public class ComputeServiceFactory extends ServiceFactorySupport implements ComputeProviderOrApiListener, ComputeProviderOrApiRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeServiceFactory.class);


    public static final String NODE_EVENT_SUPPORT = "eventsupport";
    public static final String CREDENTIAL_STORE = "credential-store";
    public static final String DEFAULT_CREDENTIAL_STORE_TYPE = "cadmin";
    public static final String CREDENTIAL_STORE_FILTER = "(&(objectClass=org.jclouds.karaf.core.CredentialStore)(credential-store-type=%s))";

    private ServiceTracker credentialStoreTracker;
    private final BundleContext bundleContext;


    public ComputeServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private CredentialStore lookupStore(String type) {
        try {
            credentialStoreTracker = new ServiceTracker(bundleContext, bundleContext.createFilter(String.format(CREDENTIAL_STORE_FILTER, type)), null);
            credentialStoreTracker.open();
            return (CredentialStore) credentialStoreTracker.waitForService(10000);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Error looking up credential store.", e);
        } catch (InterruptedException e) {
            LOGGER.error("Timed out waiting for store.", e);
        }
        return null;
    }

    public String getName() {
        return "Compute Service Factory";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        ServiceRegistration newRegistration = null;
        try {
            lock.tryLock();
            if (properties != null) {
                Properties props = new Properties();
                for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
                    Object key = e.nextElement();
                    Object val = properties.get(key);
                    props.put(key, val);
                }

                String provider = (String) properties.get(PROVIDER);
                String api = (String) properties.get(API);

                ProviderMetadata providerMetadata = null;
                ApiMetadata apiMetadata = null;

                if (!Strings.isNullOrEmpty(provider) && installedProviders.containsKey(provider)) {
                    providerMetadata = installedProviders.get(provider);
                } else if (!Strings.isNullOrEmpty(api) && installedApis.containsKey(api)) {
                    apiMetadata = installedApis.get(api);
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

                String identity = (String) properties.get(IDENTITY);
                String credential = (String) properties.get(CREDENTIAL);
                String endpoint = (String) properties.get(ENDPOINT);
                String storeType = (String) properties.get(CREDENTIAL_STORE);
                String eventSupport = (String) properties.get(NODE_EVENT_SUPPORT);
                Boolean enableEventSupport = false;

                if (storeType == null || storeType.isEmpty()) {
                    storeType = DEFAULT_CREDENTIAL_STORE_TYPE;
                }

                if (eventSupport != null && !eventSupport.isEmpty()) {
                    enableEventSupport = Boolean.parseBoolean(eventSupport);
                }

                CredentialStore credentialStore = lookupStore(storeType);

                ContextBuilder builder = null;
                if (providerMetadata != null) {
                    builder = ContextBuilder.newBuilder(providerMetadata);
                } else if (apiMetadata != null) {
                    builder = ContextBuilder.newBuilder(apiMetadata);
                }

                if (!Strings.isNullOrEmpty(endpoint)) {
                    builder = builder.endpoint(endpoint);
                }

                builder = builder.modules(ImmutableSet.<Module>of(new Log4JLoggingModule(), new SshjSshClientModule()));

                if (credentialStore != null) {
                    builder = builder.modules(ImmutableSet.<Module>of(credentialStore));
                }

                builder = builder.credentials(identity, credential).overrides(props);

                ComputeServiceContext context = builder.build(ComputeServiceContext.class);

                ComputeService service = null;

                if (enableEventSupport) {
                    service = new ComputeServiceEventProxy(bundleContext, context.getComputeService());
                } else {
                    service = context.getComputeService();
                }

                newRegistration = bundleContext.registerService(
                        ComputeService.class.getName(), service, properties);

                //If all goes well remove the pending pid.
                if (pendingPids.containsKey(pid)) {
                    activePids.put(pid, pendingPids.remove(pid));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error creating compute service.", ex);
        } finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                oldRegistration.unregister();
            }
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}