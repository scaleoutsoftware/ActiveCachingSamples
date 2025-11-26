# GeoSpatialEventTracker

## Overview

This example demonstrates how to use [ScaleOut Active Caching](https://static.scaleoutsoftware.com/docs/ac_user_guide/intro/intro.html) to create a Java MSG module for EventTracking.

The *GeoSpatialEventTracker* project contains the MSG module implementation, where `EventProcessor.java` implements the module's core logic in the abstract method ``processMessages``. See [Creating a MSG Module Project](https://static.scaleoutsoftware.com/docs/ac_user_guide/develop/java/msg_modules/create_msg_module.html) for more details on creating MSG modules.

## Requirements

This project requires Java and **Apache Maven** (version 3.9.x or higher) to build and install dependencies.

## Build and Package the Module

Run ``mvn package`` to package. 

The unit test requires a local ScaleOut StateServer installation. If you'd like to skip the tests, run:

``mvn package -DskipTests``

... this will create a ``GeoSpatialEventTracker.zip`` ZIP archive in your project's ``target`` directory.

Use the [ScaleOut Active Caching UI](https://static.scaleoutsoftware.com/docs/ac_user_guide/ui/modules.html) to upload the package and manage your deployment. Once deployed, the package will run as a worker process on every host in the ScaleOut StateServer cluster.

## Running the LoadGenerator
````
LoadGenerator
 -c,--con_string <arg>     The SOSS connection string. Default:
                           bootstrapGateways=localhost;maxPoolSize=32;
 -f,--csv_file <arg>       The CSV file needed to load event trackers.
                           Required.
 -m,--msg_second <arg>     The number of msgs per second to send. Default:
                           1000
 -n,--num_trackers <arg>   The number of event trackers. Default: 2000