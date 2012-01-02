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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.collect.ImmutableSet;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.karaf.services.modules.ConfigurationAdminCredentialStore;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public class ComputeServiceFactory implements ManagedServiceFactory {

    public static final String PROVIDER = "provider";
    public static final String IDENTITY = "identity";
    public static final String CREDENTIAL = "credential";


    private final Map<String, ServiceRegistration> registrations = new ConcurrentHashMap<String, ServiceRegistration>();

    private final BundleContext bundleContext;
    private ConfigurationAdmin configurationAdmin;
    private ServiceReference configurationAdminReference;


    public ComputeServiceFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void init() {
        configurationAdminReference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        configurationAdmin = (ConfigurationAdmin) bundleContext.getService(configurationAdminReference);
    }

    public void destroy() {
      bundleContext.ungetService(configurationAdminReference);
    }

    public String getName() {
        return "Compute Service Factory";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        System.out.println("Updating configuration properties for ComputeServiceFactory " + pid);
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
                String identity = (String) properties.get(IDENTITY);
                String credential = (String) properties.get(CREDENTIAL);

                ConfigurationAdminCredentialStore credentialStore = new ConfigurationAdminCredentialStore(configurationAdmin.getConfiguration(ConfigurationAdminCredentialStore.CREDENTIAL_STORE_PID));
                ComputeServiceContext context = new ComputeServiceContextFactory()
                        .createContext(provider, identity, credential,
                                ImmutableSet.of(new Log4JLoggingModule(), new SshjSshClientModule(), credentialStore), props);
                ComputeService client = context.getComputeService();

                newRegistration = bundleContext.registerService(
                        ComputeService.class.getName(), client, properties);
            }
        } catch (IOException ex) {
            throw new ConfigurationException("Error creating managed compute service.",ex.getMessage());
        }
        finally {
            ServiceRegistration oldRegistration = (newRegistration == null)
                    ? registrations.remove(pid)
                    : registrations.put(pid, newRegistration);
            if (oldRegistration != null) {
                System.out.println("Unregistering ComputeService " + pid);
                oldRegistration.unregister();
            }
        }
    }

    public void deleted(String pid) {
        System.out.println("ComputeServiceFactory deleted (" + pid + ")");
        ServiceRegistration oldRegistration = registrations.remove(pid);
        if (oldRegistration != null) {
            oldRegistration.unregister();
        }
    }

}
