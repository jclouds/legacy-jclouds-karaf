package org.jclouds.karaf.commands.blobstore;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

/**
 * @author: iocanel
 */
@Command(scope = "jclouds", name = "blobstore-read", description = "Reads data from the blobstore")
public class ReadCommand extends BlobStoreCommandSupport {

    @Argument(index = 0, name = "bucketName", description = "The name of the bucket", required = true, multiValued = false)
    String bucketName;

    @Argument(index = 1, name = "blobName", description = "The name of the blob", required = true, multiValued = false)
    String blobName;

    @Override
    protected Object doExecute() throws Exception {
       Object payload = read(bucketName, blobName);
       System.out.printf("%s\n", payload);
       return null;
    }
}
