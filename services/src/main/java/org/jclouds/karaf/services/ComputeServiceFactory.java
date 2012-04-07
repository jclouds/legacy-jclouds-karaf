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

import com.google.common.collect.ImmutableSet;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.karaf.core.ComputeProviderListener;
import org.jclouds.karaf.core.ComputeServiceEventProxy;
import org.jclouds.karaf.core.CredentialStore;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ComputeServiceFactory implements ManagedServiceFactory, ComputeProviderListener  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeServiceFactory.class);

    public static final String PROVIDER = "provider";
    public static final String IDENTITY = "identity";
    public static final String CREDENTIAL = "credential";
    public static final String NODE_EVENT_SUPPORT = "eventsupport";
    public static final String CREDENTIAL_STORE = "credential-store";
    public static final String DEFAULT_CREDENTIAL_STORE_TYPE = "cadmin";
    public static final String CREDENTIAL_STORE_FILTER = "(&(objectClass=org.jclouds.karaf.core.CredentialStore)(credential-store-type=%s))";


    private final Map<String, ServiceRegistration> registrations = new ConcurrentHashMap<String, ServiceRegistration>();
    private final Map<String, Dictionary> pendingPids = new HashMap<String, Dictionary>();
    private final Map<String, String> providerPids = new HashMap<String, String>();
    private final Set<String> installedProviders = new LinkedHashSet<String>();


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
            LOGGER.error("Error looking up credential store.",e);
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
            if (properties != null) {
                Properties props = new Properties();
                for (Enumeration e = properties.keys(); e.hasMoreElements();) {
                    Object key = e.nextElement();
                    Object val = properties.get(key);
                    props.put(key, val);
                }
                String provider = (String) properties.get(PROVIDER);

                if (!installedProviders.contains(provider)) {
                    pendingPids.put(pid, properties);
                    providerPids.put(provider,pid);
                    LOGGER.debug("Provider {} is not currently installed. Service will resume once the the provider is installed.", provider);
                    return;
                }

                ComputeServiceContext context = null;

                String identity = (String) properties.get(IDENTITY);
                String credential = (String) properties.get(CREDENTIAL);
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

                if (credentialStore != null) {
                 context = new ComputeServiceContextFactory()
                        .createContext(provider, identity, credential,
                                ImmutableSet.of(new Log4JLoggingModule(), new SshjSshClientModule(), credentialStore), props);
                } else {
                    context = new ComputeServiceContextFactory()
                            .createContext(provider, identity, credential,
                                    ImmutableSet.of(new Log4JLoggingModule(), new SshjSshClientModule()), props);
                }
                ComputeService client = null;

                if (enableEventSupport) {
                    client = new ComputeServiceEventProxy(bundleContext,context.getComputeService());
                } else {
                    client = context.getComputeService();
                }

                newRegistration = bundleContext.registerService(
                        ComputeService.class.getName(), client, properties);

                //If all goes well remove the pending pid.
                pendingPids.remove(pid);
            }
        } catch (Exception ex) {
            LOGGER.error("Error creating compute service.",ex);
        }
        finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                oldRegistration.unregister();
            }
        }
    }

    public void deleted(String pid) {
        ServiceRegistration oldRegistration = registrations.remove(pid);
        if (oldRegistration != null) {
            oldRegistration.unregister();
        }
    }

    @Override
    public void providerInstalled(String provider) {
        installedProviders.add(provider);
        //Check if there is a pid that requires the installed provider.
        String pid = providerPids.get(provider);
        if (pid != null) {
            Dictionary properties = pendingPids.get(pid);
            try {
                updated(pid, properties);
            } catch (ConfigurationException e) {
                LOGGER.error("Error while installing service for pending provider " + provider + " with pid "+ pid, e);
            }
        }
    }

    @Override
    public void providerUninstalled(String provider) {
        String pid = providerPids.get(provider);
        if (pid != null) {
            deleted(pid);
        }
    }

    public Set<String> getInstalledProviders() {
        return installedProviders;
    }
}