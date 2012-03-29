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

package org.jclouds.karaf.cache.compute;

import java.util.Arrays;

import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.CacheManager;
import org.jclouds.karaf.cache.Cacheable;
import org.jclouds.karaf.cache.tasks.UpdateCachesTask;

public class ComputeCacheManager extends CacheManager<ComputeService> {

    public void bindService(ComputeService service) {
        services.add(service);
        scheduledExecutorService.submit(new UpdateCachesTask(cacheables, Arrays.asList(service)));
    }

    public void unbindService(ComputeService service) {
        if (services != null) {
            this.services.remove(service);
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