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

package org.jclouds.karaf.commands.blobstore.completer;

import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;

public class BlobStoreApiCompleter implements Completer {

   private final StringsCompleter delegate = new StringsCompleter();
   private List<? extends BlobStore> blobStoreServices;

   @Override
   public int complete(String buffer, int cursor, List<String> candidates) {
      try {
         if (blobStoreServices != null) {
            for (BlobStore blobStore : blobStoreServices) {
               String id = blobStore.getContext().unwrap().getId();
               if (Apis.withId(id) != null) {
                  delegate.getStrings().add(id);
               }
            }
         }
      } catch (Exception ex) {
         // noop
      }
      return delegate.complete(buffer, cursor, candidates);
   }

   public List<? extends BlobStore> getBlobStoreServices() {
      return blobStoreServices;
   }

   public void setBlobStoreServices(List<? extends BlobStore> blobStoreServices) {
      this.blobStoreServices = blobStoreServices;
   }
}
