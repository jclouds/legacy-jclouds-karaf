/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
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
package org.jclouds.karaf.samples.s3;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * @author: iocanel
 */
public class BucketReader {

    private String accessKeyId;
    private String secretKey;

    private BlobStoreContext context;


    public BucketReader(String accessKeyId, String secretKey) {
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
        this.context = new BlobStoreContextFactory().createContext("aws-s3", accessKeyId, secretKey);
    }


    public Object read(String bucket, String blobName) {
        Object result = null;
        ObjectInputStream ois = null;

        BlobStore blobStore = context.getBlobStore();
        blobStore.createContainerInLocation(null, bucket);

        InputStream is = blobStore.getBlob(bucket, blobName).getPayload().getInput();


        try {
            ois = new ObjectInputStream(is);
            result = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
}
