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

package org.jclouds.karaf.services;

import com.google.common.base.Strings;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.karaf.core.Constants;
import org.jclouds.osgi.ApiListener;
import org.jclouds.osgi.ProviderListener;
import org.jclouds.providers.ProviderMetadata;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ServiceFactorySupport implements ManagedServiceFactory, ProviderListener, ApiListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeServiceFactory.class);


    protected final Map<String, ServiceRegistration> registrations = new ConcurrentHashMap<String, ServiceRegistration>();

    protected final Map<String, Dictionary> pendingPids = new HashMap<String, Dictionary>();
    protected final Map<String, Dictionary> activePids = new HashMap<String, Dictionary>();

    protected final Map<String, String> providerPids = new HashMap<String, String>();
    protected final Map<String, String> apiPids = new HashMap<String, String>();
    protected final Map<String, ProviderMetadata> installedProviders = new HashMap<String, ProviderMetadata>();
    protected final Map<String, ApiMetadata> installedApis = new HashMap<String, ApiMetadata>();

    protected final ReentrantLock lock = new ReentrantLock();

    public void deleted(String pid) {
        ServiceRegistration oldRegistration = registrations.remove(pid);
        if (oldRegistration != null) {
            oldRegistration.unregister();
        }
    }

    public abstract boolean apply(ProviderMetadata provider);

    public abstract boolean apply(ApiMetadata api);

    public void added(ProviderMetadata provider) {
        if (!apply(provider))  {
            return;
        }
        try {
            lock.tryLock();
            installedProviders.put(provider.getId(), provider);
            //Check if there is a pid that requires the installed provider.
            String pid = providerPids.get(provider.getId());
            if (pid != null && pendingPids.containsKey(pid)) {
                Dictionary properties = pendingPids.get(pid);
                try {
                    updated(pid, properties);
                } catch (ConfigurationException e) {
                    LOGGER.error("Error while installing service for pending provider " + provider + " with pid " + pid, e);
                }
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    public void removed(ProviderMetadata provider) {
        if (!apply(provider))  {
            return;
        }
        try {
            lock.tryLock();
            String pid = providerPids.get(provider.getId());
            if (pid != null) {
                if (activePids.containsKey(pid)) {
                    pendingPids.put(pid, activePids.remove(pid));
                }
                deleted(pid);
            }
            installedProviders.remove(provider.getId());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void added(ApiMetadata api) {
        if (!apply(api))  {
            return;
        }
        try {
            lock.tryLock();
            installedApis.put(api.getId(), api);
            //Check if there is a pid that requires the installed provider.
            String pid = apiPids.get(api.getId());
            if (pid != null && pendingPids.containsKey(pid)) {
                Dictionary properties = pendingPids.get(pid);
                try {
                    updated(pid, properties);
                } catch (ConfigurationException e) {
                    LOGGER.error("Error while installing service for pending api " + api + " with pid " + pid, e);
                }
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void removed(ApiMetadata api) {
        if (!apply(api))  {
            return;
        }
        try {
            lock.tryLock();
            String pid = apiPids.get(api.getId());
            if (pid != null) {
                if (activePids.containsKey(pid)) {
                    pendingPids.put(pid, activePids.remove(pid));
                }
                deleted(pid);
            }
            installedApis.remove(api.getId());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * Checks if configuration is valid for the specified {@link ProviderMetadata}.
     *
     * @param providerMetadata
     * @param properties
     * @throws InvalidConfigurationException
     */
    static void validate(ProviderMetadata providerMetadata, Dictionary properties) throws InvalidConfigurationException {
        if (Strings.isNullOrEmpty((String) properties.get(Constants.IDENTITY)) && !providerMetadata.getApiMetadata().getDefaultIdentity().isPresent()) {
            throw new InvalidConfigurationException("No identity specified.");
        }

        if (Strings.isNullOrEmpty((String) properties.get(Constants.CREDENTIAL)) && !providerMetadata.getApiMetadata().getDefaultCredential().isPresent()) {
            throw new InvalidConfigurationException("No credential specified specified.");
        }
    }

    /**
     * Checks if configuration is valid for the specified {@link ApiMetadata}
     *
     * @param apiMetadata
     * @param properties
     * @throws InvalidConfigurationException
     */
    static void validate(ApiMetadata apiMetadata, Dictionary properties) throws InvalidConfigurationException {
        if (Strings.isNullOrEmpty((String) properties.get(Constants.IDENTITY)) && !apiMetadata.getDefaultIdentity().isPresent()) {
            throw new InvalidConfigurationException("No identity specified.");
        }

        if (Strings.isNullOrEmpty((String) properties.get(Constants.CREDENTIAL)) && !apiMetadata.getDefaultCredential().isPresent()) {
            throw new InvalidConfigurationException("No credential specified specified.");
        }

        if (Strings.isNullOrEmpty((String) properties.get(Constants.ENDPOINT)) && !apiMetadata.getDefaultEndpoint().isPresent()) {
            throw new InvalidConfigurationException("No credential specified specified.");
        }
    }

    public Map<String, ProviderMetadata> getInstalledProviders() {
        return installedProviders;
    }

    public Map<String, ApiMetadata> getInstalledApis() {
        return installedApis;
    }

}
