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

import org.jclouds.scriptbuilder.domain.Statement;

/**
 * A RecipeManager is a repository which {@link RecipeProvider}s can bind and unbind to.
 * It also acts as a facade of the {@link RecipeManager} bound to it.
 */
public interface RecipeManager {

    /**
     * Returns the recipe that corresponds to the specified coords.
     * @param coords
     * @return
     */
    Statement createStatement(String coords, String group);

    /**
     * Binds a {@link RecipeProvider}
     * @param provider
     */
    public void bind(RecipeProvider provider);

    /**
     * Unbinds a {@link RecipeProvider}
     * @param provider
     */
    public void unibind(RecipeProvider provider);
}
