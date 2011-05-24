package org.jclouds.karaf.samples.s3;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

/**
 * @author: iocanel
 */
public class Reader {

    private static final String S3_READER_PID = "org.jclouds.samples.s3.reader";
    private static final String ACCESS_KEY_ID = "ACCESS_KEY_ID";
    private static final String SECRET_KEY = "SECRET_KEY";
    private static final String BLOB_NAME = "BLOB_NAME";
    private static final String BUCKET = "BUCKET";

    private ConfigurationAdmin configurationAdmin;


    public void init() throws IOException {
        if (configurationAdmin != null) {
            Configuration configuration = configurationAdmin.getConfiguration(S3_READER_PID);
            Dictionary properties = configuration.getProperties();

            if (properties != null) {
                String accessKeyId = (String) properties.get(ACCESS_KEY_ID);
                String secretKey = (String) properties.get(SECRET_KEY);
                String blobName = (String) properties.get(BLOB_NAME);
                String bucketName = (String) properties.get(BUCKET);


                BucketReader reader = new BucketReader(accessKeyId, secretKey);
                System.err.println(reader.read(bucketName, blobName));

            }
        }
    }

    public ConfigurationAdmin getConfigurationAdmin() {
        return configurationAdmin;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
