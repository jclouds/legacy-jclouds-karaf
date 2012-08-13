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

package org.jclouds.karaf.commands.blobstore;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-list", description = "Lists all containers")
public class BlobListCommand extends BlobStoreCommandWithOptions {

    private static final String LISTFORMAT = "%-40s %-40s";

    @Override
    protected Object doExecute() throws Exception {
        BlobStore blobStore = null;
        try {
            blobStore = getBlobStore();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return null;
        }
        System.out.println(String.format(LISTFORMAT, "[Container]", "[Blob]"));
        for (StorageMetadata containerMetadata : blobStore.list()) {
            String containerName = containerMetadata.getName();

            cacheProvider.getProviderCacheForType("container").put(containerMetadata.getProviderId(),containerName);
            PageSet<? extends StorageMetadata> blobStoreMetadatas = blobStore.list(containerName, ListContainerOptions.Builder.recursive());

            if (blobStoreMetadatas == null || !blobStoreMetadatas.isEmpty()) {
                for (StorageMetadata blobMetadata : blobStoreMetadatas) {
                    String blobName = blobMetadata.getName();
                    cacheProvider.getProviderCacheForType("blob").put(containerMetadata.getProviderId(),blobName);
                    System.out.println(String.format(LISTFORMAT, containerName, blobName));
                    containerName = "";
                }
                System.out.println(String.format(LISTFORMAT, "", ""));
            } else {
                System.out.println(String.format(LISTFORMAT, containerName, "<empty>"));
                System.out.println(String.format(LISTFORMAT, "", ""));
            }
        }
        return null;
    }
}