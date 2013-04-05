/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jclouds.karaf.commands.blobstore;

import java.io.InputStream;
import java.io.File;
import java.net.URL;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.options.PutOptions;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-write", description = "Writes data to the blobstore")
public class BlobWriteCommand extends BlobStoreCommandWithOptions {

   @Argument(index = 0, name = "containerName", description = "The name of the container", required = true, multiValued = false)
   String containerName;

   @Argument(index = 1, name = "blobName", description = "The name of the blob", required = true, multiValued = false)
   String blobName;

   @Argument(index = 2, name = "payload", description = "Payload, interpreted as a file name by default", required = true, multiValued = false)
   String payload;

   @Option(name = "-s", aliases = "--string-payload", description = "Use string payload instead of a file", required = false, multiValued = false)
   boolean stringPayload;

   @Option(name = "-u", aliases = "--url-payload", description = "Use payload from a URL instead of a file", required = false, multiValued = false)
   boolean urlPayload;

   @Option(name = "-m", aliases = "--multipart-upload", description = "Use multi-part upload", required = false, multiValued = false)
   boolean multipartUpload;

   @Override
   protected Object doExecute() throws Exception {
      BlobStore blobStore = getBlobStore();

      BlobBuilder builder = blobStore.blobBuilder(blobName);
      if (stringPayload) {
         builder = builder.payload(payload.getBytes());  // use default Charset
      } else if (urlPayload) {
         InputStream input = new URL(payload).openStream();
         try {
            builder = builder.payload(ByteStreams.toByteArray(input));
         } finally {
            input.close();
         }
      } else {
         builder = builder.payload(new File(payload)).calculateMD5();
      }

      PutOptions options = multipartUpload ? new PutOptions().multipart(true) : PutOptions.NONE;
      write(blobStore, containerName, blobName, builder.build(), options);

      cacheProvider.getProviderCacheForType("container").put(blobStore.getContext().unwrap().getId(), containerName);
      cacheProvider.getProviderCacheForType("blob").put(blobStore.getContext().unwrap().getId(), blobName);

      return null;
   }
}
