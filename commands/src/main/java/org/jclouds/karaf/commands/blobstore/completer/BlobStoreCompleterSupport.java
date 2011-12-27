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

package org.jclouds.karaf.commands.blobstore.completer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.karaf.commands.blobstore.BlobStoreHelper;

public abstract class BlobStoreCompleterSupport implements Completer, Runnable {

    private List<BlobStore> services;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected final StringsCompleter delegate = new StringsCompleter();
    protected Set<String> cache = new LinkedHashSet<String>();

    private Long lastUpdate = System.currentTimeMillis();


    @Override
    public void run() {
        updateCache();
        lastUpdate = System.currentTimeMillis();
    }

    protected BlobStore getBlobStore() {
        BlobStore service = null;
        try {
            service = BlobStoreHelper.getBlobStore(null, services);
        } catch (IllegalArgumentException ex) {
            //Ignore and skip completion;
        }
        return service;
    }

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        boolean isCached = false;

        if (System.currentTimeMillis() - lastUpdate > 60000) {
            executorService.submit(this);
        }

        delegate.getStrings().clear();
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


    protected Set<String> listContainers() {
        Set<String> containers = new LinkedHashSet<String>();
        BlobStore blobStore = getBlobStore();
        if (blobStore != null) {
            PageSet<? extends StorageMetadata> storageMetadatas = blobStore.list();
            if (storageMetadatas != null && !storageMetadatas.isEmpty()) {
                for (StorageMetadata metadata : storageMetadatas) {
                    containers.add(metadata.getName());
                }
            }
        }
        return containers;
    }

    protected Set<String> listBlobs(String container) {
        Set<String> blobs = new LinkedHashSet<String>();
        BlobStore blobStore = getBlobStore();
        if (blobStore != null) {
            if (blobStore.containerExists(container)) {
                PageSet<? extends StorageMetadata> storageMetadatas = blobStore.list(container);
                if (storageMetadatas != null && !storageMetadatas.isEmpty()) {
                    for (StorageMetadata metadata : storageMetadatas) {
                        blobs.add(metadata.getName());
                    }
                }
            }
        }
        return blobs;
    }


    public abstract void updateCache();

    public List<BlobStore> getServices() {
        return services;
    }

    public void setServices(List<BlobStore> services) {
        this.services = services;
        executorService.execute(this);
    }
}
