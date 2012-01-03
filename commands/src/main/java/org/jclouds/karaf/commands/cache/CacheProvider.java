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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheProvider {

    private static Map<String,Set<String>> caches = new ConcurrentHashMap<String, Set<String>>();

    public static synchronized Set<String> getCache(String name) {
        if  (caches.containsKey(name)) {
            return caches.get(name);
        } else {
            Set<String> cache = new LinkedHashSet<String>();
            caches.put(name, cache);
            return cache;
        }
    }
}
