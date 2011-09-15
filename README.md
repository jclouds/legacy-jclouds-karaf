# Karaf JClouds Integration

This project currently hosts a Karaf feature for easy installation of JClouds inside Apache Karaf.

Installation Instructions
-------------------------
On Karaf 2.2.0 or later:

1) Install JClouds AWS S3 Module
karaf@root> feature:addurl mvn:org.jclouds.karaf/jclouds-karaf/1.1.1-SNAPSHOT/xml/features
karaf@root> feature:install jclouds-aws-s3

2) Install Karaf Commands
karaf@root> feature:install jclouds-commands

3) Create a sample blobstore service
karaf@root> config:edit  org.jclouds.blobstore-s3
karaf@root> config:propset provider aws-s3
karaf@root> config:propset identiy XXXXXXXXX
karaf@root> config:propset identiy XXXXXXXXX
karaf@root> config:propset credential XXXXXXXXX
karaf@root> config:update

4) Use the blobstore commands
karaf@root> jclouds:blobstore-write BUCKET_NAME BLOB_NAME payload
karaf@root> jclouds:blobstore-read BUCKET_NAME BLOB_NAME
And it will display the payload

5) Create a sample ec2 compute service
karaf@root> config:edit  org.jclouds.compute-ec2
karaf@root> config:propset provider aws-ec2
karaf@root> config:propset identiy XXXXXXXXX
karaf@root> config:propset identiy XXXXXXXXX
karaf@root> config:propset credential XXXXXXXXX
karaf@root> config:propset jclouds.ec2.ami-owners  XXXXXXXXX
karaf@root> config:update

6) Use the compute service commands
karaf@root> jclouds:create --imageId YOUR_IMAGE_ID --locationId YOUR_LOCATION_ID GROUPNAME
Enjoy your new instance on EC2

The sample is basic it will just display the content of the blob to the standard output.

## License

Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>

Licensed under the Apache License, Version 2.0

