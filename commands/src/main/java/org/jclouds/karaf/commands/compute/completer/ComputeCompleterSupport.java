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

package org.jclouds.karaf.commands.compute.completer;

import java.util.List;
import java.util.Set;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.commands.cache.Cacheable;
import org.jclouds.karaf.utils.compute.ComputeHelper;

public abstract class ComputeCompleterSupport implements Completer, Cacheable<ComputeService> {

    private List<ComputeService> computeServices;

    protected final StringsCompleter delegate = new StringsCompleter();
    protected Set<String> cache;

    protected ComputeService getService() {
        ComputeService service = null;
        try {
            service = ComputeHelper.getComputeService(null, computeServices);
        } catch (IllegalArgumentException ex) {
            //Ignore and skip completion;
        }
        return service;
    }

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        delegate.getStrings().clear();
        for (String item : cache) {
            if (buffer == null || item.startsWith(buffer)) {
                delegate.getStrings().add(item);
            }
        }

        return delegate.complete(buffer, cursor, candidates);
    }

    @Override
    public void updateCache() {
        cache.clear();
        for (ComputeService computeService : computeServices) {
            updateCache(computeService);
        }
    }

    public List<ComputeService> getComputeServices() {
        return computeServices;
    }

    public void setComputeServices(List<ComputeService> computeServices) {
        this.computeServices = computeServices;
    }

    public Set<String> getCache() {
        return cache;
    }

    public void setCache(Set<String> cache) {
        this.cache = cache;
    }
}
