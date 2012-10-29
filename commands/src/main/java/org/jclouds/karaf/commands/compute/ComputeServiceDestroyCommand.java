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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@Command(scope = "jclouds", name = "compute-service-destroy", description = "Destroys a compute service", detailedDescription = "classpath:compute-service-destroy.txt")
public class ComputeServiceDestroyCommand extends ComputeCommandBase {

  @Argument(index = 0, required = true, multiValued = false, description = "The service id. Used to distinct between multiple service of the same provider/api. Only ")
  protected String id;

   @Override
   protected Object doExecute() throws Exception {
      Configuration configuration = findOrCreateFactoryConfiguration(configAdmin, "org.jclouds.compute", id, null, null);
      if (configuration != null) {
         configuration.delete();
      } else {
         System.out.println("No service found for provider / api");
      }
      return null;
   }

   public ConfigurationAdmin getConfigAdmin() {
      return configAdmin;
   }

   public void setConfigAdmin(ConfigurationAdmin configAdmin) {
      this.configAdmin = configAdmin;
   }
}
