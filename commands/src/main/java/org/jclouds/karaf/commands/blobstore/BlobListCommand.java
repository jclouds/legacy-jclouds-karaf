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

import com.google.common.collect.Lists;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-list", description = "Lists all containers")
public class BlobListCommand extends BlobStoreCommandWithOptions {

   @Argument(index = 0, name = "containerNames", description = "The name of the container", required = false, multiValued = true)
   final Collection<String> containerNames = Lists.newArrayList();

   @Option(name = "-a", aliases = "--all", description = "List all containers", required = false)
   boolean listAllContainers = false;

   private static final PrintStream out = System.out;

   @Override
   protected Object doExecute() throws Exception {
      BlobStore blobStore = null;
      try {
         blobStore = getBlobStore();
      } catch (Throwable t) {
         System.err.println(t.getMessage());
         return null;
      }

      if (listAllContainers) {
         containerNames.clear();
         for (StorageMetadata containerMetadata : blobStore.list()) {
            String containerName = containerMetadata.getName();
            containerNames.add(containerName);
            cacheProvider.getProviderCacheForType("container").put(containerMetadata.getProviderId(), containerName);
         }
      } else if (containerNames.isEmpty()) {
         throw new IllegalArgumentException("Must specify container names or --all");
      }

      for (String containerName : containerNames) {
         out.println(containerName + ":");
         out.println();

         ListContainerOptions options = ListContainerOptions.Builder.recursive();

         while (true) {
            PageSet<? extends StorageMetadata> blobStoreMetadatas = blobStore.list(containerName, options);

            for (StorageMetadata blobMetadata : blobStoreMetadatas) {
               String blobName = blobMetadata.getName();
               cacheProvider.getProviderCacheForType("blob").put(blobMetadata.getProviderId(), blobName);
               out.println("    " + blobName);
            }

            String marker = blobStoreMetadatas.getNextMarker();
            if (marker == null) {
               break;
            }

            options = options.afterMarker(marker);
         }

         out.println();
      }
      return null;
   }
}
