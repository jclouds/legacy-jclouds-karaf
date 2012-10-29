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

import java.util.List;

import com.google.common.reflect.TypeToken;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;

public class ComputeProviderCompleter implements Completer {

   private final StringsCompleter delegate = new StringsCompleter();
   private List<? extends ComputeService> computeServices;

   private final boolean displayProvidersWithoutService;

   public ComputeProviderCompleter(boolean displayProvidersWithoutService) {
     this.displayProvidersWithoutService = displayProvidersWithoutService;
   }

   @Override
   public int complete(String buffer, int cursor, List<String> candidates) {
      try {
        if (displayProvidersWithoutService) {
          for (ProviderMetadata providerMetadata : Providers.viewableAs(TypeToken.of(ComputeServiceContext.class))) {
            delegate.getStrings().add(providerMetadata.getId());
          }
        } else if (computeServices != null) {
            for (ComputeService computeService : computeServices) {
               String id = computeService.getContext().unwrap().getId();
               if (Providers.withId(id) != null) {
                  delegate.getStrings().add(computeService.getContext().unwrap().getId());
               }
            }
         }
      } catch (Exception ex) {
         // noop
      }
      return delegate.complete(buffer, cursor, candidates);
   }

   public List<? extends ComputeService> getComputeServices() {
      return computeServices;
   }

   public void setComputeServices(List<? extends ComputeService> computeServices) {
      this.computeServices = computeServices;
   }
}
