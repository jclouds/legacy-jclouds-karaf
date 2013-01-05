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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

public class RecipeProviders {

    /**
     * Returns {@link RecipeProvider} from {@link ServiceLoader}.
     *
     * @return
     */
    public static Iterable<RecipeProvider> fromServiceLoader() {
        return Iterable.class.cast(ServiceLoader.load(RecipeProvider.class));
    }

    /**
     * Returns all available {@link RecipeProvider}s.
     *
     * @return all available {@link RecipeProvider}s.
     */
    public static Iterable<RecipeProvider> all() {
        return ImmutableSet.<RecipeProvider>builder()
                .addAll(fromServiceLoader()).build();
    }

    /**
     * Returns the {@link RecipeProvider} with id.
     * @param id
     * @return
     * @throws NoSuchElementException
     */
    public static RecipeProvider withId(String id) throws NoSuchElementException {
        return Iterables.find(all(), RecipeProviderPredicates.id(id));
    }
}
