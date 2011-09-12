/**
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
package org.jclouds.karaf.commands.blobstore;

import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author iocanel
 */
public abstract class BlobStoreCommandSupport extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(BlobStoreCommandSupport.class);

    private List<BlobStore> services;

    @Option(name = "--provider")
    protected String provider;

    public void setBlobStoreServices(List<BlobStore> services) {
        this.services = services;
    }

    protected List<BlobStore> getBlobStoreServices() {
        if (provider == null) {
            return services;
        } else {
            return Collections.singletonList(getBlobStore());
        }
    }

    protected BlobStore getBlobStore() {
        if (provider != null) {
            BlobStore service = null;
            for (BlobStore svc : services) {
                if (provider.equals(service.getContext().getProviderSpecificContext().getId())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("Provider " + provider + " not found");
            }
            return service;
        } else {
            if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (BlobStore svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(svc.getContext().getProviderSpecificContext().getId());
                }
                throw new IllegalArgumentException("Multiple providers are present, please select one using the --provider argument in the following values: " + sb.toString());
            }
            return services.get(0);
        }
    }

       public Object read(String bucket, String blobName) {
        Object result = null;
        ObjectInputStream ois = null;

            BlobStore blobStore = getBlobStore();
            blobStore.createContainerInLocation(null, bucket);

            InputStream is = blobStore.getBlob(bucket, blobName).getPayload().getInput();

            try {
                ois = new ObjectInputStream(is);
                result = ois.readObject();
            } catch (IOException e) {
                logger.error("Error reading object.",e);
            } catch (ClassNotFoundException e) {
                logger.error("Error reading object.", e);
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


    public void write(String bucket, String blobName, Object object) {
            BlobStore blobStore = getBlobStore();
            Blob blob = blobStore.blobBuilder(blobName).build();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;

            try {
                oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                blob.setPayload(baos.toByteArray());
                blobStore.putBlob(bucket, blob);
            } catch (IOException e) {
                logger.error("Error while writing blob", e);
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
    }
}
