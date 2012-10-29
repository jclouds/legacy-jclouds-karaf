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

package org.jclouds.karaf.utils.compute;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Strings;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.core.Constants;

public class ComputeHelper {

  /**
   * Returns the cache keys for a given {@link ComputeService}.
   * @param computeService
   * @return
   */
  public static List<String> findCacheKeysForService(ComputeService computeService) {
    List<String> keys = new LinkedList<String>();
    String contextName = (String) computeService.getContext().unwrap().getName();
    String providerOrApi = computeService.getContext().unwrap().getId();
    if (contextName != null) {
      keys.add(contextName);
    }
    if (providerOrApi != null) {
      keys.add(providerOrApi);
    }
    return keys;
  }

    /**
     * Chooses a {@link ComputeService} that matches the specified a service id or a provider / api.
     * @param id
     * @param providerOrApi
     * @param services
     * @return
     */
    public static ComputeService getComputeService(String id, String providerOrApi, List<ComputeService> services) {
        if (!Strings.isNullOrEmpty(id)) {
          ComputeService service = null;
          for (ComputeService svc : services) {
            if (id.equals(svc.getContext().unwrap().getName())) {
              service = svc;
              break;
            }
          }
          if (service == null) {
            throw new IllegalArgumentException("No compute service with id" + id + " found.");
          }
          return service;
        }

        if (!Strings.isNullOrEmpty(providerOrApi)) {
            ComputeService service = null;
            for (ComputeService svc : services) {
                if (providerOrApi.equals(svc.getContext().unwrap().getId())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("No Provider or Api named " + providerOrApi + " found.");
            }
            return service;
        } else {
            if (services.size() == 0) {
                throw new IllegalArgumentException("No providers are present.  Note: It takes a couple of seconds for the provider to initialize.");
            }
            else if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (ComputeService svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(svc.getContext().unwrap().getId());
                }
                throw new IllegalArgumentException("Multiple providers/apis are present, please select one using the --provider/--api argument in the following values: " + sb.toString());
            } else {
                return services.get(0);
            }
        }
    }

    private ComputeHelper() {
        //Utility Class
    }
}
