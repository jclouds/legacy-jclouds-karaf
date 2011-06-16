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

import java.util.Collections;
import java.util.List;

import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.jclouds.compute.ComputeService;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class JCloudsCommandSupport extends OsgiCommandSupport {

    private List<ComputeService> services;

    @Option(name = "--provider")
    protected String provider;

    public void setServices(List<ComputeService> services) {
        this.services = services;
    }

    protected List<ComputeService> getComputeServices() {
        if (provider == null) {
            return services;
        } else {
            return Collections.singletonList(getComputeService());
        }
    }

    protected ComputeService getComputeService() {
        if (provider != null) {
            ComputeService service = null;
            for (ComputeService svc : services) {
                if (provider.equals(service.getContext().getProviderSpecificContext().getId())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("Provider " + provider + " not found");
            }
            return service;
        } else {
            if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (ComputeService svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(svc.getContext().getProviderSpecificContext().getId());
                }
                throw new IllegalArgumentException("Multiple providers are present, please select one using the --provider argument in the following values: " + sb.toString());
            }
            return services.get(0);
        }
    }
}
