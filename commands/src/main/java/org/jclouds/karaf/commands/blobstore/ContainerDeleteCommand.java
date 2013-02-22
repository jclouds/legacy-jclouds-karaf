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

import java.util.LinkedList;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.blobstore.BlobStore;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-container-delete", description = "Deletes a container")
public class ContainerDeleteCommand extends BlobStoreCommandWithOptions {

   @Argument(index = 0, name = "containerNames", description = "The name of the container", required = true, multiValued = true)
   List<String> containerNames = new LinkedList<String>();

   @Override
   protected Object doExecute() throws Exception {
      BlobStore blobStore = getBlobStore();

      for (String container : containerNames) {
         blobStore.deleteContainer(container);
         cacheProvider.getProviderCacheForType("container").remove(blobStore.getContext().unwrap().getId(),
                  container);
      }
      return null;
   }
}
