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
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.karaf.commands.cache.Cacheable;
import org.jclouds.karaf.utils.blobstore.BlobStoreHelper;

public abstract class BlobStoreCompleterSupport implements Completer, Cacheable<BlobStore> {

    private List<BlobStore> blobStoreServices;

    protected final StringsCompleter delegate = new StringsCompleter();
    protected Set<String> cache;

    protected BlobStore getBlobStore() {
        BlobStore service = null;
        try {
            service = BlobStoreHelper.getBlobStore(null, blobStoreServices);
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
        for (BlobStore blobStore : getBlobStoreServices()) {
            updateCache(blobStore);
        }
    }



    protected Set<String> listContainers() {
        Set<String> containers = new LinkedHashSet<String>();
        for (BlobStore blobStore : blobStoreServices) {
            containers.addAll(listContainers(blobStore));
        }
        return containers;
    }


     protected Set<String> listContainers(BlobStore blobStore) {
        Set<String> containers = new LinkedHashSet<String>();
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
        for (BlobStore blobStore : blobStoreServices) {
            blobs.addAll(listBlobs(blobStore, container));
        }
        return blobs;
    }

     protected Set<String> listBlobs(BlobStore blobStore, String container) {
        Set<String> blobs = new LinkedHashSet<String>();
        if (blobStore != null && blobStore.containerExists(container)) {
            PageSet<? extends StorageMetadata> storageMetadatas = blobStore.list(container);
            if (storageMetadatas != null && !storageMetadatas.isEmpty()) {
                for (StorageMetadata metadata : storageMetadatas) {
                    blobs.add(metadata.getName());
                }
            }
        }
        return blobs;
    }


    public List<BlobStore> getBlobStoreServices() {
        return blobStoreServices;
    }

    public void setBlobStoreServices(List<BlobStore> blobStoreServices) {
        this.blobStoreServices = blobStoreServices;
    }

    public Set<String> getCache() {
        return cache;
    }

    public void setCache(Set<String> cache) {
        this.cache = cache;
    }
}
