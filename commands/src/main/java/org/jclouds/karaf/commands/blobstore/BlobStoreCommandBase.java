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

import com.google.common.base.Strings;
import com.google.common.io.InputSupplier;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobStoreUtils;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.core.Constants;
import org.jclouds.providers.ProviderMetadata;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * @author iocanel
 */
public abstract class BlobStoreCommandBase extends AbstractAction {

   private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreCommandBase.class);

   public static final String FACTORY_FILTER = "(service.factoryPid=%s)";
   public static final String PROVIDERFORMAT = "%-24s %-12s %-12s";

   protected List<BlobStore> blobStoreServices = new ArrayList<BlobStore>();
   protected CacheProvider cacheProvider = new BasicCacheProvider();
   protected ConfigurationAdmin configAdmin;

   @Override
   public Object execute(CommandSession session) throws Exception {
      this.session = session;
      return doExecute();
   }

   public void setBlobStoreServices(List<BlobStore> services) {
      this.blobStoreServices = services;
   }

   protected List<BlobStore> getBlobStoreServices() {
      return blobStoreServices;
   }

  /**
   * Finds a {@link org.osgi.service.cm.Configuration} if exists, or creates a new one.
   *
   * @param configurationAdmin
   * @param factoryPid
   * @param provider
   * @param api
   * @return
   * @throws java.io.IOException
   */
  protected Configuration findOrCreateFactoryConfiguration(ConfigurationAdmin configurationAdmin, String factoryPid,
                                                           String id, String provider, String api) throws IOException {
    Configuration configuration = null;
    if (configurationAdmin != null) {
      try {
        Configuration[] configurations = configurationAdmin.listConfigurations(String.format(FACTORY_FILTER,
                factoryPid));
        if (configurations != null) {
          for (Configuration conf : configurations) {
            Dictionary<?, ?> dictionary = conf.getProperties();
            if (dictionary != null && id != null) {
              if (id.equals(dictionary.get(Constants.NAME))) {
                return conf;
              }
            } else {
              if (dictionary != null && provider != null && provider.equals(dictionary.get("provider"))) {
                return conf;
              } else if (dictionary != null && api != null && api.equals(dictionary.get("api"))) {
                return conf;
              }
            }
          }
        }
      } catch (Exception e) {
        // noop
      }
      configuration = configurationAdmin.createFactoryConfiguration(factoryPid, null);
    }
    return configuration;
  }

   /**
    * Returns an InputStream to a {@link org.jclouds.blobstore.domain.Blob}.
    * 
    * @param containerName
    * @param blobName
    * @return
    */
   public InputSupplier<InputStream> getBlobInputStream(BlobStore blobStore, String containerName, String blobName)
         throws Exception {
      Blob blob = blobStore.getBlob(containerName, blobName);
      if (blob == null) {
         if (!blobStore.containerExists(containerName)) {
            throw new ContainerNotFoundException(containerName, "while getting blob");
         }
         throw new KeyNotFoundException(containerName, blobName, "while getting blob");
      }
      return blob.getPayload();
   }

   /**
    * Writes to the {@link org.jclouds.blobstore.domain.Blob} using an InputStream.
    * 
    * @param blobStore
    * @param bucket
    * @param blobName
    * @param blob
    * @param options
    */
   public void write(BlobStore blobStore, String bucket, String blobName, Blob blob, PutOptions options) throws Exception {
      if (blobName.contains("/")) {
         String directory = BlobStoreUtils.parseDirectoryFromPath(blobName);
         if (!Strings.isNullOrEmpty(directory)) {
            blobStore.createDirectory(bucket, directory);
         }
      }

      blobStore.putBlob(bucket, blob, options);
   }

   protected void printBlobStoreProviders(Iterable<ProviderMetadata> providers, List<BlobStore> blobStores,
            String indent, PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (ProviderMetadata provider : providers) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
         for (BlobStore blobStore : blobStores) {
           String contextName = (String) blobStore.getContext().unwrap().getName();
            if (blobStore.getContext().unwrap().getId().equals(provider.getId())) {
               sb.append(contextName).append(" ");
            }
         }
         sb.append("]");
         out.println(String.format(PROVIDERFORMAT, provider.getId(), "blobstore", sb.toString()));
      }
   }

   protected void printBlobStoreApis(Iterable<ApiMetadata> apis, List<BlobStore> blobStores, String indent,
            PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (ApiMetadata api : apis) {
         StringBuilder sb = new StringBuilder();
         sb.append("[ ");
         for (BlobStore blobStore : blobStores) {
           String contextName = (String) blobStore.getContext().unwrap().getName();
            if (blobStore.getContext().unwrap().getId().equals(api.getId())) {
              sb.append(contextName).append(" ");
            }
         }
         sb.append("]");
         out.println(String.format(PROVIDERFORMAT, api.getId(), "blobstore", sb.toString()));
      }
   }

   public CacheProvider getCacheProvider() {
      return cacheProvider;
   }

   public void setCacheProvider(CacheProvider cacheProvider) {
      this.cacheProvider = cacheProvider;
   }

  public ConfigurationAdmin getConfigAdmin() {
    return configAdmin;
  }

  public void setConfigAdmin(ConfigurationAdmin configAdmin) {
    this.configAdmin = configAdmin;
  }
}
