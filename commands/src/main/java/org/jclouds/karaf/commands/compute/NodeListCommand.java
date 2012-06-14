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
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-list", description = "Displays the list of nodes.")
public class NodeListCommand extends ComputeCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = getComputeService();
        if (service == null) {
            System.out.println("Failed to find or create a compute service.");
            return null;
        }
        printNodes(service.listNodes(), "", System.out);

        for (ComputeMetadata node : service.listNodes()) {

            //Update Caches
            if (node instanceof NodeMetadata) {
                NodeMetadata metadata = (NodeMetadata) node;
                if (metadata.getState().equals(NodeState.RUNNING)) {
                    cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).put(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                } else if (metadata.getState().equals(NodeState.SUSPENDED)) {
                    cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).put(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).put(service.getContext().getProviderSpecificContext().getId(), node.getId());
                } else if (metadata.getState().equals(NodeState.TERMINATED)) {
                    cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                    cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                } else {
                    cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(service.getContext().getProviderSpecificContext().getId(), node.getId());
                }
            }
        }

        return null;
    }

}
