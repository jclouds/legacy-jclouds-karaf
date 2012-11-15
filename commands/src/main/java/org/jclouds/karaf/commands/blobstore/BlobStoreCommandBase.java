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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.util.BlobStoreUtils;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.core.Constants;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

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
      try {
         this.session = session;
         return doExecute();
      } catch (AuthorizationException ex) {
         System.err.println("Authorization error. Please make sure you provided valid identity and credential.");
         return null;
      }
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
    * Reads an Object from the blob store.
    * 
    * @param blobStore
    * @param containerName
    * @param blobName
    * @return
    */
   public Object read(BlobStore blobStore, String containerName, String blobName) {
      Object result = null;
      ObjectInputStream ois = null;

      blobStore.createContainerInLocation(null, containerName);

      InputStream is = blobStore.getBlob(containerName, blobName).getPayload().getInput();

      try {
         ois = new ObjectInputStream(is);
         result = ois.readObject();
      } catch (IOException e) {
         LOGGER.error("Error reading object.", e);
      } catch (ClassNotFoundException e) {
         LOGGER.error("Error reading object.", e);
      } finally {
         if (ois != null) {
            try {
               ois.close();
            } catch (IOException e) {
            }
         }

         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
            }
         }
      }
      return result;
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
      if (blobStore.blobExists(containerName, blobName)) {
         return blobStore.getBlob(containerName, blobName).getPayload();
      } else {
         throw new Exception("Blob " + blobName + " does not exist in container " + containerName + ".");
      }
   }

   /**
    * Writes to the {@link org.jclouds.blobstore.domain.Blob} by serializing an Object.
    * 
    * @param blobStore
    * @param containerName
    * @param blobName
    * @param object
    */
   public void write(BlobStore blobStore, String containerName, String blobName, Object object) {
      Blob blob = blobStore.blobBuilder(blobName).build();
      blob.setPayload(toBytes(object));
      blobStore.putBlob(containerName, blob);
   }

   /**
    * Writes to the {@link org.jclouds.blobstore.domain.Blob} using an InputStream.
    * 
    * @param blobStore
    * @param bucket
    * @param blobName
    * @param is
    */
   public void write(BlobStore blobStore, String bucket, String blobName, InputStream is) {
      try {
         if (blobName.contains("/")) {
            String directory = BlobStoreUtils.parseDirectoryFromPath(blobName);
            if (!Strings.isNullOrEmpty(directory)) {
               blobStore.createDirectory(bucket, directory);
            }
         }

         Blob blob = blobStore.blobBuilder(blobName).payload(ByteStreams.toByteArray(is)).build();
         blobStore.putBlob(bucket, blob);
         is.close();
      } catch (Exception ex) {
         LOGGER.warn("Error closing input stream.", ex);
      }
   }

   public byte[] toBytes(Object object) {
      byte[] result = null;

      if (object instanceof byte[]) {
         return (byte[]) object;
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = null;

      try {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
         result = baos.toByteArray();
      } catch (IOException e) {
         LOGGER.error("Error while writing blob", e);
      } finally {
         if (oos != null) {
            try {
               oos.close();
            } catch (IOException e) {
            }
         }

         if (baos != null) {
            try {
               baos.close();
            } catch (IOException e) {
            }
         }
      }
      return result;
   }

   /**
    * Reads a bye[] from a URL.
    * 
    * @param url
    * @return
    */
   public byte[] readFromUrl(URL url) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataInputStream dis = null;
      try {
         dis = new DataInputStream(url.openStream());
         int size = 0;
         while ((size = dis.available()) > 0) {
            byte[] buffer = new byte[size];
            baos.write(buffer);
         }
         return baos.toByteArray();
      } catch (IOException e) {
         LOGGER.warn("Failed to read from stream.", e);
      } finally {
         if (dis != null) {
            try {
               dis.close();
            } catch (Exception e) {
               // Ignore
            }
         }

         if (baos != null) {
            try {
               baos.close();
            } catch (Exception e) {
               // Ignore
            }
         }

      }
      return new byte[0];
   }

   protected void printBlobStoreProviders(Map<String, ProviderMetadata> providers, List<BlobStore> blobStores,
            String indent, PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (String provider : providers.keySet()) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
         for (BlobStore blobStore : blobStores) {
           String contextName = (String) blobStore.getContext().unwrap().getName();
            if (blobStore.getContext().unwrap().getId().equals(provider)) {
               sb.append(contextName).append(" ");
            }
         }
         sb.append("]");
         out.println(String.format(PROVIDERFORMAT, provider, "blobstore", sb.toString()));
      }
   }

   protected void printBlobStoreApis(Map<String, ApiMetadata> apis, List<BlobStore> blobStores, String indent,
            PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (String provider : apis.keySet()) {
         StringBuilder sb = new StringBuilder();
         sb.append("[ ");
         for (BlobStore blobStore : blobStores) {
           String contextName = (String) blobStore.getContext().unwrap().getName();
            if (blobStore.getContext().unwrap().getId().equals(provider)) {
              sb.append(contextName).append(" ");
            }
         }
         sb.append("]");
         out.println(String.format(PROVIDERFORMAT, provider, "blobstore", sb.toString()));
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
