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

package org.jclouds.karaf.cache.tasks;

import java.util.List;
import java.util.Map;

import org.jclouds.karaf.cache.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCachesTask<T> implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCachesTask.class);

    private final List<Cacheable<T>> cacheables;
    private final Map<String, T> services;
    private boolean keepRunning = true;

    public UpdateCachesTask(List<Cacheable<T>> cacheables, Map<String, T> services) {
        this.cacheables = cacheables;
        this.services = services;
    }

    @Override
    public void run() {
        if (services != null && !services.isEmpty()) {
            for (T service : services.values()) {
                if (cacheables != null && !cacheables.isEmpty()) {
                    for (Cacheable<T> cacheable : cacheables) {
                        try {
                          if (keepRunning) {
                            cacheable.updateOnAdded(service);
                          }
                        } catch (Throwable t) {
                            LOGGER.debug("Error while updating cache:" + t.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void stop() {
      keepRunning = false;
    }
}
