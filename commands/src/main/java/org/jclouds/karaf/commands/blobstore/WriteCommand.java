package org.jclouds.karaf.commands.blobstore;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-write", description = "Writes data from the blobstore")
public class WriteCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "bucketName", description = "The name of the bucket", required = true, multiValued = false)
    String bucketName;

    @Argument(index = 1, name = "blobName", description = "The name of the blob", required = true, multiValued = false)
    String blobName;

    @Argument(index = 2, name = "payload", description = "The payload", required = true, multiValued = false)
    String payload;


    @Override
    protected Object doExecute() throws Exception {
        write(bucketName, blobName, payload);
        return null;
    }
}