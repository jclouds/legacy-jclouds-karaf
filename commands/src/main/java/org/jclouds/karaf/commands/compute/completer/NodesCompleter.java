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

package org.jclouds.karaf.commands.compute.completer;

import java.util.Set;

import org.apache.karaf.shell.console.Completer;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;

public class NodesCompleter extends ComputeCompleterSupport implements Completer {

    public void init() {
        cache = cacheProvider.getProviderCacheForType("node");
    }

    @Override
    public void updateOnAdded(ComputeService computeService) {
        if (computeService != null) {
            Set<? extends ComputeMetadata> computeMetadatas = computeService.listNodes();
            if (computeMetadatas != null) {
                for (ComputeMetadata compute : computeMetadatas) {
                    NodeMetadata node = (NodeMetadata) compute;
                    if (apply(node)) {
                        cache.put(computeService.getContext().getProviderSpecificContext().getId(),node.getId());
                    }
                }
            }
        }
    }

    public boolean apply(NodeMetadata node) {
        return true;
    }
}
