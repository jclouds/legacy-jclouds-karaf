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

package org.jclouds.karaf.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jclouds.karaf.cache.tasks.UpdateCachesTask;

public class CacheManager<T> implements Runnable {

    protected ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    protected final List<Cacheable<T>> cacheables = new LinkedList<Cacheable<T>> ();
    protected final List<T> services = new LinkedList<T>();

    public void init() {
      scheduledExecutorService.scheduleAtFixedRate(this,0,5, TimeUnit.MINUTES);
    }
    public void destroy() {
      scheduledExecutorService.shutdown();
    }

    @Override
    public void run() {
        //Update all cacheables for all services.
        new UpdateCachesTask(cacheables,services).run();
    }
}
