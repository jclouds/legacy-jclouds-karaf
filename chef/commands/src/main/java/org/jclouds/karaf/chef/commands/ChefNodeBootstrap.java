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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.chef.ChefService;
import org.jclouds.chef.util.RunListBuilder;
import org.jclouds.scriptbuilder.domain.Statement;

import java.util.List;

@Command(scope = "chef", name = "node-bootstrap", description = "Bootstraps a node.")
public class ChefNodeBootstrap extends ChefRunscriptBase {


    @Option(name = "--chef-serivce", description = "The name of the chef service.", required = false, multiValued = false)
    private String chefName;

    @Argument(index = 0, name = "id", description = "The id of the node.", required = true, multiValued = false)
    private String id;

    @Argument(index = 1, name = "cookbook", description = "The cookbook.", required = true, multiValued = false)
    private String cookbook;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getScript() {
        return null;
    }

    @Override
    public Statement getStatement() {
        Statement statement = null;
        ChefService chefService = getChefService();
        if (chefService != null) {
            List<String> runlist = new RunListBuilder().addRecipes(cookbook).build();
            chefService.updateRunListForGroup(runlist, "single");
            statement = chefService.createBootstrapScriptForGroup("single");
        }
        return statement;
    }
}
