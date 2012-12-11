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

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.utils.EnvHelper;
import org.jclouds.providers.Providers;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Command(scope = "jclouds", name = "blobstore-service-create", description = "Creates a BlobStore service.", detailedDescription = "classpath:blobstore-service-create.txt")
public class BlobStoreServiceCreateCommand extends BlobStoreCommandWithOptions {

   @Option(name = "--add-option", multiValued = true, description = "Adds a key value pair to the configuration.")
   protected String[] options;

   @Option(name = "--no-wait", multiValued = true, description = "Don't wait for blob store service registration.")
   protected boolean noWait;

   private BundleContext bundleContext;

   @Override
   protected Object doExecute() throws Exception {
      if (provider == null && api == null && name == null) {
         System.err.println("You need to specify at least a valid provider or api.");
         return null;
      }

      if (name == null && provider != null) {
       name = provider;
      } else if (name == null && api != null) {
       name = api;
      }

      Map<String, String> props = parseOptions(options);
      registerBlobStore(configAdmin, name, provider, api, identity, credential, endpoint, props);
      if (noWait) {
         return null;
      } else if (!isProviderOrApiInstalled(provider, api)) {
         System.out
                  .println("Provider / api currently not installed. Service will be created once it does get installed.");
         return null;
      } else {
        System.out.println(String.format("Waiting for blostore service with name: %s.", name));
        waitForBlobStore(bundleContext, name, provider, api);
      }
      return null;
   }

   /**
    * Returns true if provider or api is currently installed.
    * 
    * @param provider
    * @param api
    * @return
    */
   private boolean isProviderOrApiInstalled(String provider, String api) {
      boolean providerOrApiFound = false;
      try {
         Providers.withId(provider);
         providerOrApiFound = true;
      } catch (Exception ex) {
         // ignore
      }
      try {
         Apis.withId(api);
         providerOrApiFound = true;
      } catch (Exception ex) {
         // ignore
      }
      return providerOrApiFound;
   }

   /**
    * Creates a {@link Map} from the specified key / value options specified.
    * 
    * @param options
    * @return
    */
   private Map<String, String> parseOptions(String[] options) {
      Map<String, String> props = new HashMap<String, String>();
      if (options != null && options.length >= 1) {
         for (String option : options) {
            if (option.contains("=")) {
               String key = option.substring(0, option.indexOf("="));
               String value = option.substring(option.lastIndexOf("=") + 1);
               props.put(key, value);
            }
         }
      }
      return props;
   }

   /**
    * Registers a {@link org.jclouds.blobstore.BlobStore}
    * 
    * 
    * @param configurationAdmin
    * @param provider
    * @param api
    * @param identity
    * @param credential
    * @param endpoint
    * @param props
    * @throws Exception
    */
   private void registerBlobStore(final ConfigurationAdmin configurationAdmin, final String id, final String provider, final String api,
            final String identity, final String credential, final String endpoint, final Map<String, String> props)
            throws Exception {
      Runnable registrationTask = new Runnable() {
         @Override
         public void run() {
            try {
               Configuration configuration = findOrCreateFactoryConfiguration(configurationAdmin,
                        "org.jclouds.blobstore", id, provider, api);
               if (configuration != null) {
                  @SuppressWarnings("unchecked")
                  Dictionary<Object, Object> dictionary = configuration.getProperties();
                  if (dictionary == null) {
                     dictionary = new Properties();
                  }

                  String providerValue = EnvHelper.getBlobStoreProvider(provider);
                  String apiValue = EnvHelper.getBlobStoreApi(api);
                  String identityValue = EnvHelper.getBlobStoreIdentity(identity);
                  String credentialValue = EnvHelper.getBlobStoreCredential(credential);
                  String endpointValue = EnvHelper.getBlobStoreEndpoint(endpoint);

                 if (id != null) {
                   dictionary.put(Constants.NAME, id);
                 }
                 if (providerValue != null) {
                   dictionary.put(Constants.PROVIDER, providerValue);
                 }
                 if (apiValue != null) {
                   dictionary.put(Constants.API, apiValue);
                 }
                 if (endpointValue != null) {
                   dictionary.put(Constants.ENDPOINT, endpointValue);
                 }
                 if (credentialValue != null) {
                   dictionary.put(Constants.CREDENTIAL, credentialValue);
                 }
                 if (identityValue != null) {
                   dictionary.put(Constants.IDENTITY, identityValue);
                 }
                  for (Map.Entry<String, String> entry : props.entrySet()) {
                     String key = entry.getKey();
                     String value = entry.getValue();
                     dictionary.put(key, value);
                  }
                  configuration.update(dictionary);
               }
            } catch (Exception ex) {
               // noop
            }
         }
      };
      new Thread(registrationTask).start();
   }

   /**
    * Waits for the {@link org.jclouds.blobstore.BlobStore} registration.
    * 
    * @param bundleContext
    * @param provider
    * @param api
    * @return
    */
   public synchronized BlobStore waitForBlobStore(BundleContext bundleContext, String name, String provider, String api) {
      BlobStore blobStore = null;
      try {
         for (int r = 0; r < 6; r++) {
            ServiceReference[] references = null;
            if (name != null) {
              references = bundleContext.getAllServiceReferences(BlobStore.class.getName(), "("+Constants.NAME+"="
                      + name + ")");
            }
            if (provider != null) {
               references = bundleContext.getAllServiceReferences(BlobStore.class.getName(), "("+Constants.PROVIDER+"=" + provider
                        + ")");
            } else if (api != null) {
               references = bundleContext.getAllServiceReferences(BlobStore.class.getName(), "("+Constants.API+"=" + api + ")");
            }
            if (references != null && references.length > 0) {
               blobStore = (BlobStore) bundleContext.getService(references[0]);
               return blobStore;
            }
            Thread.sleep(10000L);
         }
      } catch (Exception e) {
         // noop
      }
      return blobStore;
   }

   public BundleContext getBundleContext() {
      return bundleContext;
   }

   public void setBundleContext(BundleContext bundleContext) {
      this.bundleContext = bundleContext;
   }
}
