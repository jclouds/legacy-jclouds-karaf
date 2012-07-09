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

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-resume", description = "Resumes a node.")
public class NodeResumeCommand extends ComputeCommandSupport {

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
        service.resumeNode(id);
        return null;
    }
}
