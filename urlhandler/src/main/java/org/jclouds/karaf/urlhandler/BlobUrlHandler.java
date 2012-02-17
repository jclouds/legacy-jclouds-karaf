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

package org.jclouds.karaf.urlhandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.google.inject.Module;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.karaf.utils.blobstore.BlobStoreHelper;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobUrlHandler extends AbstractURLStreamHandlerService {

    private final Logger logger = LoggerFactory.getLogger(BlobUrlHandler.class);

    private static String SYNTAX = "blob:provider/container/blob";

    private List<BlobStore> blobStores = new LinkedList<BlobStore>();


    /**
     * Open the connection for the given URL.
     *
     * @param url the url from which to open a connection.
     * @return a connection on the specified URL.
     * @throws java.io.IOException if an error occurs or if the URL is malformed.
     */
    @Override
    public URLConnection openConnection(URL url) throws IOException {
        if (url.getPath() == null || url.getPath().trim().length() == 0 || !url.getPath().contains("/")) {
            throw new MalformedURLException("Container / Blob cannot be null or empty. Syntax: " + SYNTAX);
        }
        String[] parts = url.getPath().split("/");

        if (parts.length == 2 && ( url.getHost() == null || url.getHost().trim().length() == 0)) {
            throw new MalformedURLException("Provider cannot be null or empty. Syntax: " + SYNTAX);
        }

        logger.debug("Blob Protocol URL is: [" + url + "]");
        return new Connection(url);
    }

    public class Connection extends URLConnection {
        final String providerName;
        final String containerName;
        final String blobName;
        final URL url;

        public Connection(URL url) {
            super(url);
            this.url = url;
            int index = 0;
            String[] parts = url.getPath().split("/");
            if (url.getHost() == null || url.getHost().trim().length() == 0) {
                this.providerName = parts[index++];
            }  else {
                this.providerName = url.getHost();
            }
            this.containerName = parts[index++];
            StringBuilder builder = new StringBuilder();
            builder.append(parts[index++]);

            for (int i = index; i < parts.length;i++) {
                builder.append("/").append(parts[i]);
            }
            this.blobName = builder.toString();
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                BlobStore blobStore = BlobStoreHelper.getBlobStore(providerName,blobStores);
                if (blobStore == null && url.getUserInfo() != null) {
                    String userInfo = url.getUserInfo();
                    String[] ui = userInfo.split(":");
                    if (ui != null && ui.length == 2) {
                        String identity = ui[0];
                        String credential = ui[1];
                        blobStore = BlobStoreHelper.createBlobStore(providerName, identity, credential, new LinkedHashSet<Module>(), new Properties());
                        blobStores.add(blobStore);
                    }
                }
                if (blobStore == null) {
                    throw new IOException("BlobStore service not available for provider " + providerName);
                }
                if (!blobStore.containerExists(containerName)) {
                    throw new IOException("Container " + containerName + " does not exists");
                } else if (!blobStore.blobExists(containerName,blobName)) {
                    throw new IOException("Blob " + blobName + " does not exists");
                }

                Blob blob = blobStore.getBlob(containerName, blobName);

                return blob.getPayload().getInput();
            } catch (Exception e) {
                throw (IOException) new IOException("Error opening blob protocol url").initCause(e);
            }
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
           try {
               BlobStore blobStore = BlobStoreHelper.getBlobStore(providerName, blobStores);
               if (!blobStore.containerExists(containerName)) {
                    blobStore.createContainerInLocation(null,containerName);
               }

               PipedOutputStream out = new PipedOutputStream();
               PipedInputStream is = new PipedInputStream(out);
               blobStore.getBlob(containerName, blobName).setPayload(is);
               return out;
            } catch (Exception e) {
                throw (IOException) new IOException("Error opening blob protocol url").initCause(e);
            }
        }
    }


    public void setBlobStores(List<BlobStore> blobStores) {
       this.blobStores = blobStores;
    }

    public List<BlobStore> getBlobStores() {
        return blobStores;
    }
}
