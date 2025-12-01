# HashMap

## Overview

This example demonstrates how to use [ScaleOut Active Caching](https://static.scaleoutsoftware.com/docs/ac_user_guide/intro/intro.html) to create a Java API module for a HashMap. Using an API module to run the HashMap in the distributed cache significantly improves performance by reducing network and CPU overhead. Reading an entire cached HashMap from a client application will consume significant CPU and network to retrieve and commit the HashMap -- API modules avoid this problem by sending small commands "get" and "put" to the module. 

The *HashMap* project contains the API module implementation, where `SossHashMapApiProcessor.java` contains methods annotated with ``SossApiMethod``. The module's core logic is located in the methods ``get`` and ``put``. See [Creating an API Module Project](https://static.scaleoutsoftware.com/docs/ac_user_guide/develop/java/api_modules/create_api_module.html) for more details on creating API modules.

The unit test demonstrates constructing and using an API module client, implemented in ``ExampleClient.java``. This class is used to perform the get and put operations on the SOSS object. 

## Requirements

This project requires Java and **Apache Maven** (version 3.9.x or higher) to build and install dependencies.

## Build and Package the Module

Run ``mvn package`` to package. 

The unit test requires a local ScaleOut StateServer installation. If you'd like to skip the tests, run:

``mvn package -DskipTests``

... this will create a ``GeoSpatialEventTracker.zip`` ZIP archive in your project's ``target`` directory.

Use the [ScaleOut Active Caching UI](https://static.scaleoutsoftware.com/docs/ac_user_guide/ui/modules.html) to upload the package and manage your deployment. Once deployed, the package will run as a worker process on every host in the ScaleOut StateServer cluster.
