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

import java.util.LinkedList;
import java.util.List;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.karaf.commands.cache.CacheProvider;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-delete", description = "Deletes a container")
public class BlobDeleteCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "containerNames", description = "The name of the container", required = true, multiValued = true)
    List<String> containerNames = new LinkedList<String>();

    @Option(name = "-b", aliases = "--blob", multiValued = true)
    List<String> blobNames = new LinkedList<String>();


    @Override
    protected Object doExecute() throws Exception {
        for (String container : containerNames) {
            if (!blobNames.isEmpty()) {
                for (String blobName : blobNames) {
                    if (getBlobStore().blobExists(container, blobName)) {
                        getBlobStore().removeBlob(container, blobName);
                        CacheProvider.getCache("blob").remove(blobName);
                    }
                }
            } else {
                getBlobStore().deleteContainer(container);
                CacheProvider.getCache("container").remove(container);
            }
        }
        return null;
    }
}