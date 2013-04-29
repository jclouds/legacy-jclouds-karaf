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

package org.jclouds.karaf.chef.services;

import com.google.common.collect.Sets;
import org.jclouds.chef.ChefService;
import org.jclouds.chef.domain.CookbookVersion;
import org.jclouds.chef.util.RunListBuilder;
import org.jclouds.karaf.recipe.RecipeProvider;
import org.jclouds.scriptbuilder.domain.Statement;

import java.util.List;
import java.util.Set;

public class ChefRecipeProvider implements RecipeProvider {

    final ChefService chefService;
    final String id;

    public ChefRecipeProvider(ChefService chefService) {
        this.chefService = chefService;
        this.id = chefService.getContext().unwrap().getName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Statement createStatement(String recipe, String group) {
        List<String> runlist = new RunListBuilder().addRecipes(recipe).build();
        chefService.updateRunListForGroup(runlist, group);
        return chefService.createBootstrapScriptForGroup(group);
    }

    @Override
    public Set<String> listProvidedRecipes() {
        Set<String> recipes = Sets.newHashSet();
        for (CookbookVersion cookbookVersion : chefService.listCookbookVersions()) {
            for (String recipe : cookbookVersion.getMetadata().getProviding().keySet()) {
                recipes.add(id + "/" + recipe);
            }
        }
        return recipes;
    }
}
