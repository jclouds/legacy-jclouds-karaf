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

package org.jclouds.karaf.recipe;

import com.google.common.collect.Maps;
import org.jclouds.scriptbuilder.domain.Statement;

import java.util.Map;
import java.util.NoSuchElementException;

public class RecipeManagerImpl implements RecipeManager {

    Map<String, RecipeProvider> recipeProviderMap = Maps.newConcurrentMap();

    public Statement createStatement(String coords, String group) throws NoSuchElementException {
        if (coords != null && coords.contains("/")) {
            String id = coords.substring(0, coords.indexOf("/"));
            String recipe = coords.substring(coords.indexOf("/") + 1);
            if (recipeProviderMap.containsKey(id)) {
                RecipeProvider provider = recipeProviderMap.get(id);
                return provider.createStatement(recipe, group);
            } else {
                RecipeProvider provider = RecipeProviders.withId(id);
                return provider.createStatement(recipe, group);
            }
        } else {
            throw new IllegalArgumentException("Recipe coords should have the following format <recipe provider>/<recipe>");
        }
    }

    public void bind(RecipeProvider provider) {
        recipeProviderMap.put(provider.getId(), provider);
    }

    public void unibind(RecipeProvider provider) {
        recipeProviderMap.remove(provider.getId());
    }
}
