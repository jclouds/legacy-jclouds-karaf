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

import com.google.common.collect.Multimap;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.ArgumentCompleter;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.apache.karaf.shell.console.jline.CommandSessionHolder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.cache.Cacheable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class BlobStoreCompleterSupport implements Completer, Cacheable<BlobStore> {

   private static final String ID_OPTION = "--id";
   private static final String PROVIDER_OPTION = "--provider";
   private static final String API_OPTION = "--api";

   protected final StringsCompleter delegate = new StringsCompleter();
   protected Multimap<String, String> cache;
   protected CacheProvider cacheProvider;

   @Override
   public int complete(String buffer, int cursor, List<String> candidates) {
     CommandSession commandSession = CommandSessionHolder.getSession();
     ArgumentCompleter.ArgumentList list = (ArgumentCompleter.ArgumentList) commandSession.get(ArgumentCompleter.ARGUMENTS_LIST);
     delegate.getStrings().clear();

     if (list != null) {
       String serviceId = extractServiceId(list.getArguments());
       String providerOrApi = extractProviderOrApiFromArguments(list.getArguments());
       Collection<String> values;

       if (serviceId != null && cache.containsKey(serviceId)) {
         values = cache.get(serviceId);
       } else if (providerOrApi != null && cache.containsKey(providerOrApi)) {
         values = cache.get(providerOrApi);
       } else {
         values = cache.values();
       }

       for (String item : values) {
         if (buffer == null || item.startsWith(buffer)) {
           delegate.getStrings().add(item);
         }
       }
     }

     return delegate.complete(buffer, cursor, candidates);
   }

  /**
   * Parses the arguemnts and extracts the service id.
   * @param args
   * @return
   */
  private String extractServiceId(String... args) {
    String id = null;
    if (args != null && args.length > 0) {
      List<String> arguments = Arrays.asList(args);
      if (arguments.contains(ID_OPTION)) {
        int index = arguments.indexOf(ID_OPTION);
        if (arguments.size() > index) {
          return arguments.get(index + 1);
        }
      }
    }
    return id;
  }

  /**
   * Parses the arguments and extracts the provider or api option value
   * @param args
   * @return
   */
  private String extractProviderOrApiFromArguments(String... args) {
    String id = null;
    if (args != null && args.length > 0) {
      List<String> arguments = Arrays.asList(args);
      if (arguments.contains(ID_OPTION)) {
        int index = arguments.indexOf(ID_OPTION);
        if (arguments.size() > index) {
          return arguments.get(index + 1);
        }
      }
      if (arguments.contains(PROVIDER_OPTION)) {
        int index = arguments.indexOf(PROVIDER_OPTION);
        if (arguments.size() > index) {
          return arguments.get(index + 1);
        }
      } else if (arguments.contains(API_OPTION)) {
        int index = arguments.indexOf(API_OPTION);
        if (arguments.size() > index) {
          return arguments.get(index + 1);
        }
      }
    }
    return id;
  }

   protected Set<String> listContainers(BlobStore blobStore) {
      Set<String> containers = new LinkedHashSet<String>();
      if (blobStore != null) {
         PageSet<? extends StorageMetadata> storageMetadatas = blobStore.list();
         if (storageMetadatas != null && !storageMetadatas.isEmpty()) {
            for (StorageMetadata metadata : storageMetadatas) {
               containers.add(metadata.getName());
            }
         }
      }
      return containers;
   }

   protected Set<String> listBlobs(BlobStore blobStore, String container) {
      Set<String> blobs = new LinkedHashSet<String>();
      if (blobStore != null && blobStore.containerExists(container)) {
         PageSet<? extends StorageMetadata> storageMetadatas = blobStore.list(container);
         if (storageMetadatas != null && !storageMetadatas.isEmpty()) {
            for (StorageMetadata metadata : storageMetadatas) {
               blobs.add(metadata.getName());
            }
         }
      }
      return blobs;
   }

   @Override
   public void updateOnRemoved(BlobStore blobStore) {
      cache.removeAll(blobStore.getContext().unwrap().getId());
   }

   public Multimap<String, String> getCache() {
      return cache;
   }

   public void setCache(Multimap<String, String> cache) {
      this.cache = cache;
   }

   public CacheProvider getCacheProvider() {
      return cacheProvider;
   }

   public void setCacheProvider(CacheProvider cacheProvider) {
      this.cacheProvider = cacheProvider;
   }
}
