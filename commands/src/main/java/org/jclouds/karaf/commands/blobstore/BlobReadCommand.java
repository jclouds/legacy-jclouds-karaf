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


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.jclouds.blobstore.BlobStore;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-read", description = "Reads data from the blobstore")
public class BlobReadCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "containerName", description = "The name of the container", required = true, multiValued = false)
    String containerName;

    @Argument(index = 1, name = "blobName", description = "The name of the blob", required = true, multiValued = false)
    String blobName;

    @Option(name = "-f", aliases = "--to-file", description = "The file to store the blob", required = false, multiValued = false)
    String file;

    @Option(name = "-d", aliases = "--display", description = "Display the content to the console", required = false, multiValued = false)
    Boolean display;

    @Override
    protected Object doExecute() throws Exception {
        BlobStore blobStore = getBlobStore();
        if (blobStore == null) {
            System.out.println("Failed to find or create a blob store.");
            return null;
        }

        if (!Strings.isNullOrEmpty(file)) {
            File f = new File(file);
            if (!f.exists() && f.createNewFile()) {
                ByteStreams.copy(getBlobInputStream(blobStore, containerName, blobName), new FileOutputStream(f));
            }
        }

        if (display) {
            InputStream inputStream = getBlobInputStream(blobStore, containerName, blobName);
            System.err.println(new String(ByteStreams.toByteArray(inputStream)));
        }
        return null;
    }
}
