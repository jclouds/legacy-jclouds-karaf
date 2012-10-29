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
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.collect.Sets;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-info", description = "Displays detailed information about a node.")
public class NodeInfoCommand extends ComputeCommandWithOptions {

   @Argument(name = "id", description = "The id of the node.", required = true, multiValued = false)
   private String id;

   @Override
   protected Object doExecute() throws Exception {
      ComputeService service = null;
      try {
         service = getComputeService();
      } catch (Throwable t) {
         System.err.println(t.getMessage());
         return null;
      }

      NodeMetadata node = service.getNodeMetadata(id);
      printNodeInfo(service, Sets.newHashSet(node), true, System.out);
      return null;
   }
}
