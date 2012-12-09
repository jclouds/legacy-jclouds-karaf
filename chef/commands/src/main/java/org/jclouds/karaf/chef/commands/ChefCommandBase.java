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

package org.jclouds.karaf.chef.commands;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.chef.ChefService;
import org.jclouds.chef.domain.CookbookVersion;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.chef.core.ChefConstants;
import org.jclouds.karaf.commands.table.ShellTable;
import org.jclouds.karaf.commands.table.ShellTableFactory;
import org.jclouds.karaf.commands.table.internal.PropertyShellTableFactory;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.utils.ServiceHelper;
import org.jclouds.rest.AuthorizationException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;


public abstract class ChefCommandBase extends AbstractAction {

    public static final String PROVIDERFORMAT = "%-24s %-12s %-12s";
    public static final String FACTORY_FILTER = "(service.factoryPid=%s)";

    protected ConfigurationAdmin configAdmin;
    protected CacheProvider cacheProvider = new BasicCacheProvider();
    protected List<ChefService> chefServices = new ArrayList<ChefService>();
    protected ShellTableFactory shellTableFactory = new PropertyShellTableFactory();

    @Override
    public Object execute(CommandSession session) throws Exception {
        try {
            this.session = session;
            return doExecute();
        } catch (AuthorizationException ex) {
            System.err.println("Authorization error. Please make sure you provided valid identity and credential.");
            return null;
        }
    }

    protected void printChefApis(Iterable<ApiMetadata> apis, List<ChefService> chefServices, PrintStream out) {
        out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
        for (ApiMetadata api : apis) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            for (ChefService chefService : chefServices) {
                String contextName = (String) chefService.getContext().getName();
                if (chefService.getContext().getId().equals(api.getId()) && contextName != null) {
                    sb.append(contextName).append(" ");
                }
            }
            sb.append("]");
            out.println(String.format(PROVIDERFORMAT, api.getId(), "compute", sb.toString()));
        }
    }

    protected void printCookbooks(ChefService service, Iterable<? extends CookbookVersion> cookbookVersions, PrintStream out) {
        ShellTable table = shellTableFactory.build("cookbook");
        table.setDisplayData(cookbookVersions);
        table.display(out, true, true);

        for (CookbookVersion cookbookVersion : cookbookVersions) {
            for (String cacheKey : ServiceHelper.findCacheKeysForService(service)) {
                cacheProvider.getProviderCacheForType(ChefConstants.COOKBOOK_CACHE).putAll(cacheKey, cookbookVersion.getMetadata().getProviding().keySet());
            }
        }
    }

    /**
     * Finds a {@link org.osgi.service.cm.Configuration} if exists, or creates a new one.
     *
     * @param configurationAdmin
     * @param factoryPid
     * @param api
     * @return
     * @throws java.io.IOException
     */
    protected Configuration findOrCreateFactoryConfiguration(ConfigurationAdmin configurationAdmin, String factoryPid, String id, String api) throws IOException {
        Configuration configuration = null;
        if (configurationAdmin != null) {
            try {
                Configuration[] configurations = configurationAdmin.listConfigurations(String.format(FACTORY_FILTER, factoryPid));
                if (configurations != null) {
                    for (Configuration conf : configurations) {
                        Dictionary<?, ?> dictionary = conf.getProperties();
                        //If id has been specified only try to match by id, ignore the rest.
                        if (dictionary != null && id != null) {
                            if (id.equals(dictionary.get(Constants.NAME))) {
                                return conf;
                            }
                        } else {
                            if (dictionary != null && api != null && api.equals(dictionary.get("api"))) {
                                return conf;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // noop
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

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public List<ChefService> getChefServices() {
        return chefServices;
    }

    public void setChefServices(List<ChefService> chefServices) {
        this.chefServices = chefServices;
    }

    public ShellTableFactory getShellTableFactory() {
        return shellTableFactory;
    }

    public void setShellTableFactory(ShellTableFactory shellTableFactory) {
        this.shellTableFactory = shellTableFactory;
    }
}
