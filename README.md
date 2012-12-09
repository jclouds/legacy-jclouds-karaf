# Karaf jclouds Integration

This project currently hosts a Karaf feature repository for easy installation of jclouds inside Apache Karaf. It also provides Managed Service Factories for creating Compute and BlobStore services. Last but not least it provides a rich command set for using jclouds from the Karaf shell.

There is also support for using chef via [jclouds-chef](https://github.com/jclouds/jclouds-chef/)  integration.

Usage Instructions
===================
The instructions will make use of Amazon EC2 and S3, but can be applied to any provider or api supported by jclouds.

On Karaf 2.2.5 or later:

Install jclouds AWS Modules
----------------------------
Install the feature and a provider for blobstore and compute service:
   
    karaf@root> features:addurl mvn:org.jclouds.karaf/jclouds-karaf/1.5.0/xml/features
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

Chef
----
You can install the chef api, with the following command:

    karaf@root> features:install jclouds-chef-api

Managed Service Factories and commands are also provided for Chef. The managed service factory allows you to create a reusable service just by passing the configuration. To install the managed serivce factories and the chef commands, you need to install the jclouds-chef feature:

    karaf@root>features:install jclouds-chef

Then you can create a chef service, using the chef:service-create command:

    karaf@root>chef:service-create  --api chef --client-name CLIENT --validator-name VALIDATOR --client-key-file CLIENT.pem --validator-key-file VALIDATOR.pem --endpoint ENDPOINT

**OPSCODE Chef Example:**
The above command for opscode chef, with client iocanel and validator iocanel-validator, the command looks like:

    karaf@root>chef:service-create  --api chef --client-name iocanel --validator-name iocanel-validator --client-key-file /Users/iocanel/.chef/iocanel.pem --validator-key-file /Users/iocanel/.chef/iocanel-validator.pem --endpoint https://api.opscode.com/organizations/iocanel

Once the service has been create, you can list your cookbooks using:

    karaf@root>chef:cookbook-list

**Using the Chef Serivce with any Provider / Api:**
Once you have created the chef service and have made sure a couple of cookbooks are uploaded. You can use chef with any other compute service in your system.
In the exmaple above it will be used with EC2:

    karaf@root>node-create --imageId eu-west-1/ami-c1aaabb5 --hardwareId m1.medium --adminAccess

    [id]                 [location] [hardware] [group]   [status]
    eu-west-1/i-bbb5eff0 eu-west-1c m1.medium  karafchef RUNNING

    karaf@root>chef:node-bootstrap  eu-west-1/i-bbb5eff0 java::openjdk



Using multiple serives per provider/api
---------------------------------------

As of jclouds-karaf 1.5.0 you are able to register multiple compute and blobstore services per provider or api. The commands will allow you to specify which serivce to use (just specifying provider/api isn't enough since we have multiple services).
To "name" the service, you can use the --id option in the serivce create commands. If no id is specified the provider/api name will be used instead.

For compute services:

    jclouds:compute-service-create --id aws1 --provider aws-ec2 ...
    jclouds:node-list --id aws1


This can be very usefull when you want to configure either different accounts per provider/api or use different configuration options. A small example:

    jclouds:compute-service-create --id aws-eu-west-1 --provider aws-ec2 --add-option jclouds.regions=eu-west-1
    jclouds:compute-service-create --id aws-us-east-1 --provider aws-ec2 --add-option jclouds.regions=us-east-1

The available ids are now shown in the compute-service-list commands:

    jclouds:compute-service-list

    Compute Providers:
    ------------------
    [id]                     [type]       [service]
    aws-ec2                  compute      [ aws-eu-west-1 aws-us-east-1 ]


To destroy one of the two available services:

    jclouds:compute-service-destroy aws-us-east-1
    jclouds:compute-service-list

    Compute Providers:
    ------------------
    [id]                     [type]       [service]
    aws-ec2                  compute      [ aws-eu-west-1 ]


Blobstore services work in a very similar manner:

    jclouds:blobstore-service-create --id s3-1 --provider aws-s3 ...


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
* **JCLOUDS_USER** The username of that will be used for accessing compute instances.
* **JCLOUDS_PASSWORD** The password that will be used for accessing compute instances.

The same pattern can be used for blobstore services to. Just replace COMPUTE with BLOBSTORE.

Configuring command output
--------------------------
As of jclouds-karaf version 1.5.0-beta.11_1 jclouds-karaf commands support output customization. The customization features are:

* **Width calculation** The commands calculate the required column width and adjust the format accordingly.
* **Configurable columns** Can add remove columns using configuration.
* **Groovy value retrieval** The display content is configurable using groovy expressions.
* **Configurable column alignment** You can configure for each column left or right alignment.
* **Configurable sorting options** Configure ordering by column using ascending or descending order.

The configuration for all columns can be found inside the org.jclouds.shell pid. Each configuration key is prefixed using the command category (node, image, location, hardware etc).
The suffix defines the configuration topic. For example hardware.headers defines the headers to be displayed by the hardware commands.
In the following commands the hardware category will be used as example.

**Defining the command headers**
To specify the headers of a command we need to place to specify the headers configuration as a semicoln separated list.
For hardware:


    hardware.headers=[id];[ram];[cpu];[cores]


**Defining the display data**
Display data are configured as a comma separated list of expressions (using the scripting engine of your choice, default is groovy). The expressions will be evaluated on the object of interest (in our example the hardware object).
To display the id field of the hardware object the expression to use is hardware.id. The reason for choosing groovy (as a default) for retrieving the data and not a simple expression language is that groovy is powerfull and can be used for more complex expressions.
For example the Hardware object contains a collection of Processors and each processor has a filed of cores. To display the sum of cores among processors, we can use the following expression: hardware.processors.sum{it.cores}.

You can change the scripting engine:

    hardware.engine=groovy

Please note that if you don't specify the engine, then groovy will be assumed.

To specify the display data, now all you need to do is to provide the expressions:

    hardware.expressions=hardware.id;hardware.ram;hardware.processors.sum{it.cores*it.speed};hardware.processors.sum{it.cores}

The configuration above will display the hardware id in the first column, the hardware ram in the second column, the sum of cores X speed per processor in the third column and finally the sum of cores for all processors in the last column.

**Defining the sort order**
To specify the sort column, the sortBy option can be used to point to the header of the column of interest.
For example hardware hardware.shortby=[cpu].

**Changing the delimeter**
Most of the configuration options for the shell table are passed as delimited strings. What happens when you want to change the delimiter?
By default the delimeter is the semicoln symbol, but for each command category you can specify the delimiter. For example:


    hardware.delimeter=,
    hardware.headers=[id],[ram],[cpu],[cores]


Using jclouds-karaf with the OBR features of Karaf
--------------------------------------------------
There are cases were there are small discrepancies between the jclouds-karaf required bundles and the ones that are used in your project. Even though inside OSGi you can have multiple versions of a bundle, it often doesn't make sense for micro versions. 

To avoid that you can install the obr feature of Karaf before installing jclouds-karaf. The obr feature among others provides the obr resolver, which will try to check if osgi package requirements are satisfied by existing bundles, before installing new bundles.

For example, assuming that a given version of jclouds-karaf is using jersey 1.11 and in your containers version 1.13 is already installed, the obr resolver will check if the 1.13 version can satisfy your needs and if so it will skip the installation of 1.11.

Code completion
---------------

Most of the commands support tab completion, in order to help the user easily complete node ids, images, locations, blob containers etc.


## License

Copyright (C) 2009-2011 jclouds, Inc.

Licensed under the Apache License, Version 2.0
