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
package org.jclouds.karaf.commands;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Hardware;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public class HardwaresCommand extends JCloudsCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        for (ComputeService service : getComputeServices()) {
            for (Hardware hardware : service.listHardwareProfiles()) {
                System.out.println(hardware.toString());
            }
        }
        return null;
    }

}
