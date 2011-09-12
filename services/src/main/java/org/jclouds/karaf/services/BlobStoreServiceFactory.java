/**
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
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BlobStoreServiceFactory implements ManagedServiceFactory {

    private final Map<String, ServiceRegistration> registrations =
            new ConcurrentHashMap<String, ServiceRegistration>();

    private final BundleContext bundleContext;

    public BlobStoreServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getName() {
        return "BlobStore Service Factory";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        System.out.println("Updating configuration properties for BlobStoreServiceFactory " + pid);
        ServiceRegistration newRegistration = null;
        try {
            if (properties != null) {
                Properties props = new Properties();
                for (Enumeration e = properties.keys(); e.hasMoreElements();) {
                    Object key = e.nextElement();
                    Object val = properties.get(key);
                    props.put(key, val);
                }
                String provider = (String) properties.get("provider");
                String identity = (String) properties.get("identity");
                String credential = (String) properties.get("credential");
                BlobStoreContext context =  new BlobStoreContextFactory().createContext(provider, identity, credential,  ImmutableSet.of(new Log4JLoggingModule(), new JschSshClientModule()), props);

                BlobStore blobStore = context.getBlobStore();
                newRegistration = bundleContext.registerService(
                        BlobStore.class.getName(), blobStore, properties);
            }
        } finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                System.out.println("Unregistering BlobStoreService " + pid);
                oldRegistration.unregister();
            }
        }

    }

    public void deleted(String pid) {
        System.out.println("BlobStoreServiceFactory deleted (" + pid + ")");
        ServiceRegistration oldRegistration = registrations.remove(pid);
        if (oldRegistration != null) {
            oldRegistration.unregister();
        }
    }
}
