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

import java.net.URL;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.karaf.commands.cache.CacheProvider;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-write", description = "Writes data to the blobstore")
public class BlobWriteCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "containerName", description = "The name of the container", required = true, multiValued = false)
    String containerName;

    @Argument(index = 1, name = "blobName", description = "The name of the blob", required = true, multiValued = false)
    String blobName;

    @Argument(index = 2, name = "payload", description = "A url pointing to a payload, or just a string payload", required = true, multiValued = false)
    String payload;

    @Option(name = "-s", aliases = "--store-url", description = "Option to store in the blob the url itself", required = false, multiValued = false)
    boolean storeUrl;

    @Override
    protected Object doExecute() throws Exception {
        URL url = null;
        try {
            url = new URL(payload);
        } catch (Exception e) {
            //Ignore
        }
        if (url == null || storeUrl) {
            write(containerName, blobName, payload);
        } else {
            write(containerName, blobName, url.openStream());
            CacheProvider.getCache("container").remove(containerName);
            CacheProvider.getCache("blob").remove(blobName);
        }
        return null;
    }
}