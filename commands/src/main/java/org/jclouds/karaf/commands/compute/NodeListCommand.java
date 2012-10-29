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
import org.apache.felix.gogo.commands.Option;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.utils.compute.ComputeHelper;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-list", description = "Displays the list of nodes.")
public class NodeListCommand extends ComputeCommandWithOptions {

  @Option(name = "-g", aliases = "--group", multiValued = false, required = false, description = "Node group")
  private String group;

  @Override
  protected Object doExecute() throws Exception {
    ComputeService service = null;
    try {
      service = getComputeService();
    } catch (Throwable t) {
      System.err.println(t.getMessage());
      return null;
    }

    Set<? extends NodeMetadata> nodes = service.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
      @Override
      public boolean apply(@Nullable ComputeMetadata input) {
        NodeMetadata node = (NodeMetadata) input;
        if (!Strings.isNullOrEmpty(group) && !group.equals(node.getGroup())) {
          return false;
        }
        return true;
      }
    });
    printNodes(service, nodes, System.out);

    for (ComputeMetadata node : service.listNodes()) {

      // Update Caches
      if (node instanceof NodeMetadata) {
        NodeMetadata metadata = (NodeMetadata) node;
        for (String cacheKey : ComputeHelper.findCacheKeysForService(service)) {
          if (metadata.getStatus().equals(NodeMetadata.Status.RUNNING)) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).put(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(
                    cacheKey, node.getId());
          } else if (metadata.getStatus().equals(NodeMetadata.Status.SUSPENDED)) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).put(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).put(
                    cacheKey, node.getId());
          } else if (metadata.getStatus().equals(NodeMetadata.Status.TERMINATED)) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(
                    cacheKey, node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(
                    cacheKey, node.getId());
          } else {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(
                    cacheKey, node.getId());
          }
        }
      }
    }

    return null;
  }

}
