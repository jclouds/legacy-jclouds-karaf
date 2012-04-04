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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Predicate;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "group-destroy")
public class GroupDestroyCommand extends ComputeCommandSupport {

    @Argument(index = 0, name = "group", description = "The group of nodes.", required = true, multiValued = false)
    private String group;

    @Override
    protected Object doExecute() throws Exception {
         Set<? extends NodeMetadata> nodeMetadatas = getComputeService().destroyNodesMatching(new Predicate<NodeMetadata>() {
            @Override
            public boolean apply(@Nullable NodeMetadata input) {
                return input.getGroup().contains(group);
            }
        });

        if (nodeMetadatas != null && !nodeMetadatas.isEmpty()) {
            System.out.println("Destroyed nodes:");
            printNodes(nodeMetadatas, "", System.out);
        }

        for (NodeMetadata node : nodeMetadatas) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(getComputeService().getContext().getProviderSpecificContext().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(getComputeService().getContext().getProviderSpecificContext().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(getComputeService().getContext().getProviderSpecificContext().getId(), node.getId());
        }
        return null;
    }
}
