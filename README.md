# Karaf JClouds Integration

This project currently hosts a Karaf feature for easy installation of JClouds inside Apache Karaf.

Usage Instructions
===================
On Karaf 2.2.0 or later:

Install JClouds AWS Modules
----------------------------
Install the feature and a provider for blobstore and compute service:
   
    karaf@root> features:addurl mvn:org.jclouds.karaf/jclouds-karaf/1.3.0-SNAPSHOT/xml/features
    karaf@root> features:install jclouds-aws-s3
    karaf@root> features:install jclouds-aws-ec2

Install Karaf Commands:
    
    karaf@root> features:install jclouds-commands


BlobStore
----------
Jclouds Karaf provides Managed Service Factories for blobstores, so creating a BlobStore Service is as easy as creating
configuration.

Create a sample blobstore service, by using the console:
    
    karaf@root> config:edit  org.jclouds.blobstore-s3
    karaf@root> config:propset provider aws-s3
    karaf@root> config:propset identity XXXXXXXXX
    karaf@root> config:propset credential XXXXXXXXX
    karaf@root> config:update

You can use the shell commands to list, create, delete, read or write to a blob:
    
    karaf@root> jclouds:blobstore-write BUCKET_NAME BLOB_NAME payload
    karaf@root> jclouds:blobstore-read BUCKET_NAME BLOB_NAME

This works well for String payloads, but for binary payloads the user can use the url to be used as input or output for the commands:

    karaf@root> jclouds:blobstore-write BUCKET_NAME BLOB_NAME URL_POINTING_TO_THE_PAYLOAD.
    karaf@root> jclouds:blobstore-read BUCKET_NAME BLOB_NAME LOCAL_FILE_TO_STORE_THE_BLOB.

If the payload represents a URI the content of the URL will be written instead.
You can bypass this by specifying the <i>--store-url</i> and store the url as a string.
 
BlobStore URL Handler
---------------------
The commands above are usefull when using the shell, but most of the time you will want the use of blobstore to be transparent.
Jclouds Karaf also provides a url handler which will allow you to use blobstore by using URLs of the following format:

<b>blob:/PROVIDER/CONTAINER/BLOB</b>

<b>A Funny Example:</b>
You can copy a bundle to a blob and install it directly from there:
    
    karaf@root> features:install jclouds-url-handler
    karaf@root>osgi:install -s blob:/PROVIDER/CONTAINER/PATH_TO_BUNDLE 


Compute Service
---------------
Managed Service Factories are also provided for ComputeService. Again all you need to do is to create a configuration
and the service will automatically created and exported for you.

    karaf@root> config:edit  org.jclouds.compute-ec2
    karaf@root> config:propset provider aws-ec2
    karaf@root> config:propset identity XXXXXXXXX
    karaf@root> config:propset credential XXXXXXXXX
    karaf@root> config:propset jclouds.ec2.ami-owners  XXXXXXXXX
    karaf@root> config:update

Use the compute service commands
    
    karaf@root> jclouds:node-create YOUR_IMAGE_ID YOUR_LOCATION_ID GROUPNAME
    karaf@root> jclouds:node-list.
    
<b>Note:<b> You can supply additional options to select hardware etc.    


Run a script to a single node or a group of nodes:
    
    karaf@root> jclouds:group-runscript --script-url URL_OF_THE_SCRIPT GROUPNAME.
    karaf@root> jclouds:node-runscript --script-url URL_OF_THE_SCRIPT NODEID.
    

Shutdown all your nodes or the nodes of a specific group:

    karaf@root> jclouds:group-destroy GROUPNAME
    karaf@root> jclouds:node-destroy-all GROUPNAME



Code completion
---------------

Most of the commands support tab completion, in order to help the user easily complete node ids, images, locations, blob containers etc.


## License

Copyright (C) 2009-2011 jclouds, Inc.

Licensed under the Apache License, Version 2.0
