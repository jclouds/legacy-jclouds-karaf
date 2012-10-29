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

package org.jclouds.karaf.cache.compute;

import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.CacheManager;
import org.jclouds.karaf.cache.Cacheable;
import org.jclouds.karaf.cache.tasks.UpdateCachesTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ComputeCacheManager extends CacheManager<ComputeService> {

    public void bindService(ComputeService service) {
        Map<String, ComputeService> map = new HashMap<String, ComputeService>();
        map.put(service.getContext().unwrap().getId(), service);
        services.putAll(map);
        scheduledExecutorService.submit(new UpdateCachesTask(cacheables, map));
    }

    public void unbindService(ComputeService service) {
        if (services != null && service != null) {
            this.services.remove(service.getContext().unwrap().getId());
        }
    }

    public  void bindCachable(Cacheable<ComputeService> cacheable) {
        this.cacheables.add(cacheable);
        scheduledExecutorService.submit(new UpdateCachesTask(Arrays.asList(cacheable), services));
    }

    public void unbindCachable(Cacheable<ComputeService> cacheable) {
        if (cacheables != null) {
            this.cacheables.remove(cacheable);
        }
    }
}