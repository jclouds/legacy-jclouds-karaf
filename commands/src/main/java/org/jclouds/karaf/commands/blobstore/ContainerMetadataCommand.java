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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.domain.Location;

/**
 * Output container metadata.
 *
 * @author: Andrew Gaul
 */
@Command(scope = "jclouds", name = "blobstore-container-metadata", description = "Output container metadata")
public class ContainerMetadataCommand extends BlobStoreCommandWithOptions {

   @Argument(index = 0, name = "containerName", description = "The name of the container", required = true, multiValued = true)
   final Collection<String> containerNames = Lists.newArrayList();

   private static final PrintStream out = System.out;

   @Override
   protected Object doExecute() throws Exception {
      BlobStore blobStore = getBlobStore();

      PageSet<? extends StorageMetadata> allContainerMetadata = blobStore.list();
      for (String containerName : containerNames) {
         boolean found = false;
         for (StorageMetadata containerMetadata : allContainerMetadata) {
            if (containerName.equals(containerMetadata.getName())) {
               printContainerMetadata(containerMetadata);
               found = true;
               break;
            }
         }
         if (!found) {
            throw new ContainerNotFoundException(containerName, "while getting container metadata");
         }
      }

      return null;
   }

   private static void printContainerMetadata(StorageMetadata containerMetadata) {
      out.println(containerMetadata.getName());
      printMetadata("ETag", containerMetadata.getETag());
      printMetadata("Creation-Date", containerMetadata.getCreationDate());
      printMetadata("Last-Modified", containerMetadata.getLastModified());
      Location location = containerMetadata.getLocation();
      if (location != null) {
         printMetadata("Location", location.getId());
      }
      printMetadata("Provider-ID", containerMetadata.getProviderId());
      printMetadata("URI", containerMetadata.getUri());
      for (Map.Entry<String, String> entry : containerMetadata.getUserMetadata().entrySet()) {
         printMetadata(entry.getKey(), entry.getValue());
      }
      out.println("");
   }

   private static void printMetadata(String key, Object value) {
      if (value != null) {
         out.println(String.format("    %s: %s", key, value));
      }
   }
}

