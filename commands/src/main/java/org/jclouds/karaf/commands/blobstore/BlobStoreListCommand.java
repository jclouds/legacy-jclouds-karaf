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
import org.jclouds.karaf.core.BlobStoreProviderOrApiRegistry;

@Command(scope = "jclouds", name = "blobstore-service-list", description = "List available BlobStore services.", detailedDescription = "classpath:blobstore-service-list.txt")
public class BlobStoreListCommand extends BlobStoreCommandBase {

    private BlobStoreProviderOrApiRegistry blobStoreProviderOrApiRegistry;

    @Override
    protected Object doExecute() throws Exception {
        try {
            if (blobStoreProviderOrApiRegistry.getInstalledApis() != null && !blobStoreProviderOrApiRegistry.getInstalledApis().isEmpty()) {
                System.out.println("BlobStore APIs:");
                System.out.println("---------------");
                printBlobStoreApis(blobStoreProviderOrApiRegistry.getInstalledApis(), getBlobStoreServices(), "", System.out);
            } else {
                System.out.println("No blob store APIs found.");
            }

            System.out.println();
            System.out.println();

            System.out.println("BlobStore Providers:");
            System.out.println("--------------------");
            if (blobStoreProviderOrApiRegistry.getInstalledProviders() != null && !blobStoreProviderOrApiRegistry.getInstalledProviders().isEmpty()) {
                printBlobStoreProviders(blobStoreProviderOrApiRegistry.getInstalledProviders(), getBlobStoreServices(), "", System.out);
            } else {
                System.out.println("No blob store providers found.");
            }
        } catch (Exception ex) {
            //noop
        }
        return null;
    }

    public BlobStoreProviderOrApiRegistry getBlobStoreProviderOrApiRegistry() {
        return blobStoreProviderOrApiRegistry;
    }

    public void setBlobStoreProviderOrApiRegistry(BlobStoreProviderOrApiRegistry blobStoreProviderOrApiRegistry) {
        this.blobStoreProviderOrApiRegistry = blobStoreProviderOrApiRegistry;
    }
}
