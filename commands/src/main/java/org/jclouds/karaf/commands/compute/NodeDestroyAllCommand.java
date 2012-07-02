/*
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

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Predicate;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-destroy-all", description = "Destroys all nodes.")
public class NodeDestroyAllCommand extends ComputeCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = getComputeService();
        if (service == null) {
            System.out.println("Failed to find or create a compute service.");
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
            printNodes(nodeMetadatas, "", System.out);
        }

        for (NodeMetadata node : nodeMetadatas) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(service.getContext().unwrap().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(service.getContext().unwrap().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(service.getContext().unwrap().getId(), node.getId());
        }
        return null;
    }
}
