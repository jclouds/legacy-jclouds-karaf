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
