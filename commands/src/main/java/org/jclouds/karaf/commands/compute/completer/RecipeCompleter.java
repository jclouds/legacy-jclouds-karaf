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

package org.jclouds.karaf.commands.compute.completer;

import org.jclouds.karaf.cache.Cacheable;
import org.jclouds.karaf.commands.support.GenericCompleterSupport;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.recipe.RecipeProvider;

import java.util.List;

public class RecipeCompleter extends GenericCompleterSupport<RecipeProvider, String> implements Cacheable<RecipeProvider> {

    private static final String ANY = "ANY";

    public void init() {
        cache = cacheProvider.getProviderCacheForType(Constants.RECIPE_CACHE);
    }
    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {        delegate.getStrings().clear();

        for (String item : cache.values()) {
            if (buffer == null || item.startsWith(buffer)) {
                delegate.getStrings().add(item);
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }


    @Override
    public String getCacheableKey(RecipeProvider type) {
        return ANY;
    }

    @Override
    public void updateOnAdded(RecipeProvider recipeProvider) {
        if (recipeProvider != null) {
            for (String recipe : recipeProvider.listProvidedRecipes()) {
                cache.put(ANY, recipe);
            }
        }
    }
}
