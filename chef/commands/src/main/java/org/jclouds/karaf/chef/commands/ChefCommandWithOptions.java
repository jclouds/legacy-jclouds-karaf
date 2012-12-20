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

package org.jclouds.karaf.chef.commands;

import com.google.common.base.Strings;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.apis.Apis;
import org.jclouds.chef.ChefService;
import org.jclouds.karaf.chef.core.ChefHelper;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ChefCommandWithOptions extends ChefCommandBase {

    @Option(name = "--name", description = "The service context name. Used to distinct between multiple service of the same provider/api. Only ")
    protected String name;

    @Option(name = "--api", description = "The api or use.")
    protected String api = "chef";

    @Option(name = "--client-name", description = "The name of the client.")
    protected String clientName;

    @Option(name = "--client-key-file", description = "The path to the client key file.")
    protected String clientKeyFile;

    @Option(name = "--validator-name", description = "The name of the validator.")
    protected String validatorName;

    @Option(name = "--validator-key-file", description = "The path to the validator key file.")
    protected String validatorKeyFile;

    @Option(name = "--endpoint", description = "The endpoint to use for a chef service.")
    protected String endpoint;

    @Override
    public List<ChefService> getChefServices() {
        if (api == null) {
            return chefServices;
        } else {
            try {
                ChefService service = getChefService();
                return Collections.singletonList(service);
            } catch (Throwable t) {
                return Collections.emptyList();
            }
        }
    }

    protected ChefService getChefService() {
        return ChefHelper.findOrCreateChefService(api, name, clientName,null, clientKeyFile, validatorName, null, validatorKeyFile, endpoint, chefServices);
    }
}
