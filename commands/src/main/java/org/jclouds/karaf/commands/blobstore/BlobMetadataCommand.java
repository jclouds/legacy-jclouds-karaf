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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.io.ContentMetadata;

/**
 * Print blob metadata.
 *
 * @author: Andrew Gaul
 */
@Command(scope = "jclouds", name = "blobstore-blob-metadata", description = "Print blob metadata")
public class BlobMetadataCommand extends BlobStoreCommandWithOptions {

   @Argument(index = 0, name = "containerName", description = "The name of the container", required = true)
   String containerName;

   @Argument(index = 1, name = "blobNames", description = "The name of the blobs", required = true, multiValued = true)
   List<String> blobNames = Lists.newArrayList();

   private static final PrintStream out = System.out;

   @Override
   protected Object doExecute() throws Exception {
      BlobStore blobStore = getBlobStore();

      for (String blobName : blobNames) {
         BlobMetadata blobMetadata = blobStore.blobMetadata(containerName, blobName);
         if (blobMetadata == null) {
            throw new FileNotFoundException("Blob does not exist: " + blobName);
         }

         ContentMetadata contentMetdata = blobMetadata.getContentMetadata();
         out.println(blobName + ":");

         printMetadata("Content-Disposition", contentMetdata.getContentDisposition());
         printMetadata("Content-Encoding", contentMetdata.getContentEncoding());
         printMetadata("Content-Language", contentMetdata.getContentLanguage());
         byte[] contentMD5 = contentMetdata.getContentMD5();
         if (contentMD5 != null) {
            printMetadata("Content-MD5",
                  BaseEncoding.base16().lowerCase().encode(contentMD5));
         }
         printMetadata("Content-Type", contentMetdata.getContentType());
         printMetadata("Expires", contentMetdata.getExpires());
         printMetadata("Length", contentMetdata.getContentLength());

         out.println("");
      }
      return null;
   }

   private static void printMetadata(String key, Object value) {
      if (value != null) {
         out.println(String.format("    %s: %s", key, value));
      }
   }
}
