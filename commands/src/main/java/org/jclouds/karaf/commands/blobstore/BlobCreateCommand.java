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

import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.compute.ComputeService;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-create", description = "Creates a container")
public class BlobCreateCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "containerNames", description = "The name of the container", required = true, multiValued = true)
    List<String> containerNames;

    @Override
    protected Object doExecute() throws Exception {
        BlobStore blobStore = getBlobStore();
        if (blobStore == null) {
            System.out.println("Failed to find or create a blob store.");
            return null;
        }
        for (String container : containerNames) {
            blobStore.createContainerInLocation(null, container);
        }
        return null;
    }
}