/**
 * Copyright (C) 2011, the original authors
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.karaf.commands.compute;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "provider-info")
public class ProviderInfoCommand extends ComputeCommandWithOptions {

   @Override
   protected Object doExecute() throws Exception {
      for (ComputeService service : getComputeServices()) {
         String txt = "Instances on " + service.getContext().unwrap().getId();
         System.out.println(txt);
         for (int i = 0; i < txt.length(); i++) {
            System.out.print('=');
         }
         System.out.println();

         System.out.println("  Locations");
         System.out.println("  ---------");
         printLocations(service,  System.out);

         System.out.println("  Images");
         System.out.println("  ------");
         printImages(service.listImages(),  System.out);

         System.out.println("  Hardware");
         System.out.println("  --------");
         printHardwares(service.listHardwareProfiles(),  System.out);

         System.out.println("  Nodes");
         System.out.println("  -----");
         printNodes(service.listNodes(), System.out);
      }
      return null;
   }

}
