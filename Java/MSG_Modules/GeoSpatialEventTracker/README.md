# GeoSpatialEventTracker

## Requirements

This project requires Java and **Apache Maven** (version 3.9.x or higher) to build and install dependencies.

## Build and Package the Module

Run ``mvn package`` to package. 

The unit test requires a local ScaleOut StateServer installation. If you'd like to skip the tests, run:

``mvn package -DskipTests``

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