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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.commands.compute.ComputeHelper;

public abstract class ComputeCompleterSupport implements Completer, Runnable  {

    private List<ComputeService> services;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected final StringsCompleter delegate = new StringsCompleter();
    protected Set<String> cache = new LinkedHashSet<String>();

    @Override
    public void run() {
        updateCache();
    }

    protected ComputeService getService() {
        ComputeService service = null;
        try {
            service = ComputeHelper.getComputeService(null, services);
        } catch (IllegalArgumentException ex) {
            //Ignore and skip completion;
        }
        return service;
    }

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        boolean isCached = false;

        for (String item : cache) {
            if (buffer == null || item.startsWith(buffer)) {
                delegate.getStrings().add(item);
                isCached = true;
            }
        }

        if (!isCached) {
            updateCache();
            //Do an other try.
            for (String item : cache) {
                if (buffer == null || item.startsWith(buffer)) {
                    delegate.getStrings().add(item);
                }
            }

        }
        return delegate.complete(buffer, cursor, candidates);
    }


    public abstract void updateCache();

    public List<ComputeService> getServices() {
        return services;
    }

    public void setServices(List<ComputeService> services) {
        this.services = services;
        executorService.execute(this);
    }
}
