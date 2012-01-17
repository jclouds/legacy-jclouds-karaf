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

package org.jclouds.karaf.commands.cache;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jclouds.blobstore.BlobStore;

public class CacheManager<T> implements Runnable {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private List<? extends Cacheable<T>> cacheables;

    public void init() {
      scheduledExecutorService.scheduleAtFixedRate(this,0,5, TimeUnit.MINUTES);
    }
    public void destroy() {
      scheduledExecutorService.shutdown();
    }

    @Override
    public void run() {
        if (cacheables != null && !cacheables.isEmpty()) {
            for(Cacheable cacheable:cacheables) {
                cacheable.updateCache();
            }
        }
    }

    public void bind(T service) {
       scheduledExecutorService.submit(this);
    }

    public void unbind(T service) {

    }

    public List<? extends Cacheable<T>> getCacheables() {
        return cacheables;
    }

    public void setCacheables(List<? extends Cacheable<T>> cacheables) {
        this.cacheables = cacheables;
    }
}
