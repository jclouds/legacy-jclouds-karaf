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

package org.jclouds.karaf.commands.compute;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.karaf.core.ComputeProviderOrApiRegistry;

@Command(scope = "jclouds", name = "compute-service-list", description = "Lists the Compute APIs and Providers", detailedDescription = "classpath:compute-service-list.txt")
public class ComputeServiceListCommand extends ComputeCommandBase {

   private ComputeProviderOrApiRegistry computeProviderOrApiRegistry;

   @Override
   protected Object doExecute() throws Exception {
      try {
         if (computeProviderOrApiRegistry.getInstalledApis() != null
                  && !computeProviderOrApiRegistry.getInstalledApis().isEmpty()) {
            System.out.println("Compute APIs:");
            System.out.println("-------------");
            printComputeApis(computeProviderOrApiRegistry.getInstalledApis(), getComputeServices(), "", System.out);
         } else {
            System.out.println("No compute APIs found.");
         }

         System.out.println();
         System.out.println();

         System.out.println("Compute Providers:");
         System.out.println("------------------");
         if (computeProviderOrApiRegistry.getInstalledProviders() != null
                  && !computeProviderOrApiRegistry.getInstalledProviders().isEmpty()) {
            printComputeProviders(computeProviderOrApiRegistry.getInstalledProviders(), getComputeServices(), "",
                     System.out);
         } else {
            System.out.println("No compute providers found.");
         }

      } catch (Exception ex) {
         // noop
      }
      return null;
   }

   public ComputeProviderOrApiRegistry getComputeProviderOrApiRegistry() {
      return computeProviderOrApiRegistry;
   }

   public void setComputeProviderOrApiRegistry(ComputeProviderOrApiRegistry computeProviderOrApiRegistry) {
      this.computeProviderOrApiRegistry = computeProviderOrApiRegistry;
   }
}
