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

package org.jclouds.karaf.services;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.karaf.core.BlobStoreProviderListener;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlobStoreServiceFactory implements ManagedServiceFactory, BlobStoreProviderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreServiceFactory.class);

    public static final String PROVIDER = "provider";
    public static final String IDENTITY = "identity";
    public static final String CREDENTIAL = "credential";


    private final Map<String, ServiceRegistration> registrations = new ConcurrentHashMap<String, ServiceRegistration>();
    private final Map<String, Dictionary> pendingPids = new HashMap<String, Dictionary>();
    private final Map<String, String> providerPids = new HashMap<String, String>();
    private final Map<String, ProviderMetadata> installedProviders = new HashMap<String, ProviderMetadata>();


    private final BundleContext bundleContext;
    private AbstractModule credentialStore;


    public BlobStoreServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getName() {
        return "BlobStore Service Factory";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        System.out.println("Updating configuration properties for BlobStore " + pid);
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

                if (!installedProviders.containsKey(provider)) {
                    pendingPids.put(pid, properties);
                    LOGGER.debug("Provider {} is not currently installed. Service will resume once the the provider is installed.", provider);
                    return;
                }

                String identity = (String) properties.get(IDENTITY);
                String credential = (String) properties.get(CREDENTIAL);

                ProviderMetadata metadata = installedProviders.get(provider);
                BlobStoreContext context = ContextBuilder.newBuilder(metadata)
                    .credentials(identity, credential)
                    .modules(ImmutableSet.<Module> of(new Log4JLoggingModule()))
                    .overrides(props)
                    .build(BlobStoreContext.class);

                BlobStore blobStore = context.getBlobStore();
                newRegistration = bundleContext.registerService(
                        BlobStore.class.getName(), blobStore, properties);

                //If all goes well remove the pending pid.
                pendingPids.remove(pid);
            }
        } catch (Exception ex) {
            LOGGER.error("Error creating blobstore service.",ex);
        }
        finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                System.out.println("Unregistering BlobStore " + pid);
                oldRegistration.unregister();
            }
        }
    }

    public void deleted(String pid) {
        System.out.println("BlobStore deleted (" + pid + ")");
        ServiceRegistration oldRegistration = registrations.remove(pid);
        if (oldRegistration != null) {
            oldRegistration.unregister();
        }
    }

    @Override
    public void providerInstalled(ProviderMetadata provider) {
        installedProviders.put(provider.getId(), provider);
        //Check if there is a pid that requires the installed provider.
        String pid = providerPids.get(provider.getId());
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
    public void providerUninstalled(ProviderMetadata provider) {
        String pid = providerPids.get(provider.getId());
        if (pid != null) {
            deleted(pid);
        }
    }

    public Map<String, ProviderMetadata> getInstalledProviders() {
        return installedProviders;
    }
}