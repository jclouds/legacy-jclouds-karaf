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
import org.jclouds.karaf.commands.cache.CacheProvider;

public class GroupCompleter extends ComputeCompleterSupport implements Completer {

    public GroupCompleter() {
        cache = CacheProvider.getCache("group");
    }

    @Override
    public void updateCache() {
        cache.clear();
        ComputeService service = getService();
        if (service != null) {
            Set<? extends ComputeMetadata> computeMetadatas = service.listNodes();
            if (computeMetadatas != null) {
                for (ComputeMetadata compute : computeMetadatas) {
                    NodeMetadata node = (NodeMetadata) compute;
                    if (apply(node)) {
                        cache.add(node.getGroup());
                    }
                }
            }
        }
    }

    public boolean apply(NodeMetadata node) {
        return true;
    }
}
