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

import org.apache.felix.gogo.commands.Option;
import org.jclouds.chef.ChefService;
import org.jclouds.karaf.chef.core.ChefHelper;
import org.jclouds.karaf.commands.compute.RunScriptBase;

import java.util.List;

public abstract class ChefRunscriptBase extends RunScriptBase {

    @Option(name = "--chef-api", description = "The name of the chef api.", required = false, multiValued = false)
    private String chefApi;
    @Option(name = "--chef-serivce", description = "The name of the chef service.", required = false, multiValued = false)
    private String chefName;

    protected List<ChefService> chefServices;

    protected ChefService getChefService() {
        if ((name == null && api == null) && (chefServices != null && chefServices.size() == 1)) {
            return chefServices.get(0);
        }
        ChefService chefService = chefService = ChefHelper.getChefService(chefName, chefApi, chefServices);
        return chefService;
    }
    public List<ChefService> getChefServices() {
        return chefServices;
    }

    public void setChefServices(List<ChefService> chefServices) {
        this.chefServices = chefServices;
    }

    @Override
    public boolean runAsRoot() {
        return true;
    }
}
