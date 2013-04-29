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

package org.jclouds.karaf.chef.commands.completer;

import com.google.common.reflect.TypeToken;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.ChefService;

import java.util.List;

public class ChefApiCompleter implements Completer {

    private final StringsCompleter delegate = new StringsCompleter();
    private List<? extends ChefService> chefServices;

    private final boolean displayApisWithoutService;

    public ChefApiCompleter(boolean displayApisWithoutService) {
        this.displayApisWithoutService = displayApisWithoutService;
    }

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        try {
            if (displayApisWithoutService) {
                for (ApiMetadata apiMetadata : Apis.contextAssignableFrom(TypeToken.of(ChefContext.class))) {
                    delegate.getStrings().add(apiMetadata.getId());
                }
            } else if (chefServices != null) {
                for (ChefService chefService : chefServices) {
                    String id = chefService.getContext().unwrap().getId();
                    if (Apis.withId(id) != null) {
                        delegate.getStrings().add(id);
                    }
                }
            }
        } catch (Exception ex) {
            // noop
        }
        return delegate.complete(buffer, cursor, candidates);
    }

    public List<? extends ChefService> getChefServices() {
        return chefServices;
    }

    public void setChefServices(List<? extends ChefService> chefServices) {
        this.chefServices = chefServices;
    }
}
