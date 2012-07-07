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

package org.jclouds.karaf.commands.compute;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

public abstract class ComputeServiceCommand extends ComputeCommandSupport {

    protected static final String FACTORY_FILTER = "(service.factoryPid=%s)";

    protected ConfigurationAdmin configAdmin;

    /**
     * Finds a {@link org.osgi.service.cm.Configuration} if exists, or creates a new one.
     *
     * @param configurationAdmin
     * @param factoryPid
     * @param provider
     * @param api
     * @return
     * @throws java.io.IOException
     */
    protected Configuration findOrCreateFactoryConfiguration(ConfigurationAdmin configurationAdmin, String factoryPid, String provider, String api) throws IOException {
        Configuration configuration = null;
        if (configurationAdmin != null) {
            try {
                Configuration[] configurations = configurationAdmin.listConfigurations(String.format(FACTORY_FILTER, factoryPid));
                if (configurations != null) {
                    for (Configuration conf : configurations) {
                        Dictionary dictionary = conf.getProperties();
                        if (dictionary != null && provider != null && provider.equals(dictionary.get("provider"))) {
                            return conf;
                        } else if (dictionary != null && api != null && api.equals(dictionary.get("api"))) {
                            return conf;
                        }
                    }
                }
            } catch (Exception e) {
                //noop
            }
            configuration = configurationAdmin.createFactoryConfiguration(factoryPid, null);
        }
        return configuration;
    }

    public ConfigurationAdmin getConfigAdmin() {
        return configAdmin;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }
}
