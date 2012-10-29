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

import java.util.Set;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Predicate;
import org.jclouds.karaf.core.Constants;
import static org.jclouds.karaf.utils.compute.ComputeHelper.findCacheKeysForService;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-destroy-all", description = "Destroys all nodes.")
public class NodeDestroyAllCommand extends ComputeCommandWithOptions {

   @Override
   protected Object doExecute() throws Exception {
      ComputeService service = null;
      try {
         service = getComputeService();
      } catch (Throwable t) {
         System.err.println(t.getMessage());
         return null;
      }
      Set<? extends NodeMetadata> nodeMetadatas = service.destroyNodesMatching(new Predicate<NodeMetadata>() {
         @Override
         public boolean apply(@Nullable NodeMetadata input) {
            return true;
         }
      });

      if (nodeMetadatas != null && !nodeMetadatas.isEmpty()) {
         System.out.println("Destroyed nodes:");
         printNodes(service, nodeMetadatas, System.out);
      }

      for (NodeMetadata node : nodeMetadatas) {
        for (String cacheKey : findCacheKeysForService(service)) {
          cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(cacheKey, node.getId());
          cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(cacheKey, node.getId());
          cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(cacheKey, node.getId());
        }
      }
      return null;
   }
}
