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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;

import java.util.List;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-destroy", description = "Destroys the specified nodes.")
public class NodeDestroyCommand extends ComputeCommandSupport {

    @Argument(name = "id", description = "The ids of the nodes to destroy.", required = true, multiValued = true)
    private List<String> ids;

    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = null;
        try {
            service = getComputeService();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return null;
        }

        for (String id : ids) {
            service.destroyNode(id);
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).remove(service.getContext().unwrap().getId(), id);
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).remove(service.getContext().unwrap().getId(), id);
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).remove(service.getContext().unwrap().getId(), id);
        }
        return null;
    }
}
