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

import com.google.common.collect.Multimap;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.cache.Cacheable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class BlobStoreCompleterSupport implements Completer, Cacheable<BlobStore> {

    protected final StringsCompleter delegate = new StringsCompleter();
    protected Multimap<String,String> cache;
    protected CacheProvider cacheProvider;


    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        delegate.getStrings().clear();
        for (String item : cache.values()) {
            if (buffer == null || item.startsWith(buffer)) {
                delegate.getStrings().add(item);
            }
        }
        return delegate.complete(buffer, cursor, candidates);
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

    @Override
    public void updateOnRemoved(BlobStore blobStore) {
        cache.removeAll(blobStore.getContext().getProviderSpecificContext().getId());
    }


    public Multimap<String,String> getCache() {
        return cache;
    }

    public void setCache(Multimap<String,String> cache) {
        this.cache = cache;
    }

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }
}
