# Karaf jclouds Integration

This project currently hosts a Karaf feature repository for easy installation of jclouds inside Apache Karaf. It also provides Managed Service Factories for creating Compute and BlobStore services. Last but not least it provides a rich command set for using jclouds from the Karaf shell.

Usage Instructions
===================
The instructions will make use of Amazon EC2 and S3, but can be applied to any provider or api supported by jclouds.

On Karaf 2.2.5 or later:

Install jclouds AWS Modules
----------------------------
Install the feature and a provider for blobstore and compute service:
   
    karaf@root> features:addurl mvn:org.jclouds.karaf/jclouds-karaf/1.5.0-SNAPSHOT/xml/features
    karaf@root> features:install jclouds-aws-s3
    karaf@root> features:install jclouds-aws-ec2

Install Karaf Commands:
    
    karaf@root> features:install jclouds-commands

To see the list of available jclouds modules for Karaf

    karaf@root> features:list | grep jclouds

Compute Service
---------------
Jclouds Karaf provides Managed Service Factories for ComputeService.
There are currently two ways of creating a compute service:

* **Using the jclouds:compute-service-create command**
* **By manually creating the configuration**

**Using the jclouds:compute-service-create command**

The compute service command allows you to create a reusable compute service for a jclouds provider or api.
To create a compute service for the EC2 provider:

    karaf@root> jclouds:compute-service-create --provider aws-ec2 --identity XXXXXX --credential XXXXXXX

and for creating a compute service using an api, for example openstack:

    karaf@root> jclouds:compute-service-create --api openstack-nova --endpoint XXXXXXXX --identity XXXXXX --credential XXXXXXX

Note that when using apis you usually need to also specify the endpoint too. The command also supports adding extra options
as key/value pairs. For example:

    karaf@root> jclouds:compute-service-create --api openstack-nova --endpoint XXXXXXXX --identity XXXXXX --credential XXXXXXX --add-option jclouds.keystone.credential-type=passwordCredentials

To see the list of installed providers and apis or remove the service for one of the providers, you can use the jclouds:compute-service-list and jclouds-compute-service-remove commands.

**Using the Karaf config commands**

To create a compute service using the Karaf's integration with the configuration admin all that needs to be done is to create a configuration with fabctory pid: org.jclouds.compute.

    karaf@root> config:edit  org.jclouds.compute-ec2
    karaf@root> config:propset provider aws-ec2
    karaf@root> config:propset identity XXXXXXXXX
    karaf@root> config:propset credential XXXXXXXXX
    karaf@root> config:propset jclouds.ec2.ami-owners  XXXXXXXXX
    karaf@root> config:update

Use the compute service commands
    
    karaf@root> jclouds:node-create --imageId YOUR_IMAGE_ID --locationId YOUR_LOCATION_ID GROUPNAME
    karaf@root> jclouds:node-list.

If you don't want/need to specify specific image, you specify the os family and the os version

    karaf@root> jclouds:node-create --os-family OS_FAMILY --os-version OS_VERSION --locationId YOUR_LOCATION_ID GROUPNAME
    
<b>Note:<b> You can supply additional options to select hardware etc.    


Run a script to a single node or a group of nodes:
    
    karaf@root> jclouds:group-runscript --script-url URL_OF_THE_SCRIPT GROUPNAME.
    karaf@root> jclouds:node-runscript --script-url URL_OF_THE_SCRIPT NODEID.

For simple commands you can just inline the command, for example to get the uptime of the node:

    karaf@root> jclouds:group-runscript --direct uptime GROUPNAME.
    karaf@root> jclouds:node-runscript --direct uptime NODEID.

Or you can use whatever command you want.

Shutdown all your nodes or the nodes of a specific group:

    karaf@root> jclouds:group-destroy GROUPNAME
    karaf@root> jclouds:node-destroy-all GROUPNAME


BlobStore
----------
There are currently two ways of creating a service for blobstore service:

* **Using the jclouds:blobstore-service-create-command**
* **By manually creating the configuration**

**Using the jclouds:blobstore-service-create command**

The compute service command allows you to create and reuse blobstore service for a jclouds provider or api.
To create a compute service for the S3 provider:

    karaf@root> jclouds:blobstore-service-create --provider aws-s3 blobstore --identity XXXXXX --credential XXXXXXX

To see the list of installed providers and apis or remove the service for one of the providers, you can use the jclouds:blobstore-service-list and jclouds-blobstore-service-remove commands.

**Using the Karaf config commands**
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


Using environmental variables
-----------------------------
When it comes to creating a service, you usually need to specify a provider/api, identity, credentials and endpoint. You can either
specify them using the command options as shown above, or pull them from your environment, if the corresponding environmental variables are found.
Supported variables:

* **JCLOUDS_COMPUTE_PROVIDER** The name of the compute provider.
* **JCLOUDS_COMPUTE_API** The name of the compute api.
* **JCLOUDS_COMPUTE_IDENTITY** The identiy for accessing the compute provider.
* **JCLOUDS_COMPUTE_CREDENTIAL** The credential for accessing the compute provider.
* **JCLOUDS_COMPUTE_ENDPOINT** The endpoint (This is usally needed when using compute apis).
* **JCLOUDS_USER ** The username of that will be used for accessing compute instances.
* **JCLOUDS_PASSWORD ** The password that will be used for accessing compute instances.

The same pattern can be used for blobstore services to. Just replace COMPUTE with BLOBSTORE.

Code completion
---------------

Most of the commands support tab completion, in order to help the user easily complete node ids, images, locations, blob containers etc.


## License

Copyright (C) 2009-2011 jclouds, Inc.

Licensed under the Apache License, Version 2.0
