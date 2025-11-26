# .NET Flight Message Module Example

## Overview

This example demonstrates how to use [ScaleOut Active Caching](https://static.scaleoutsoftware.com/docs/ac_user_guide/intro/intro.html) to create a .NET message module representing an airline flight.

The *Flight* project contains the message module implementation, where `FlightMessageHandler.cs` implements the module's core logic in its `ProcessMessageAsync()` method. See [Creating a Message Module Project](https://static.scaleoutsoftware.com/docs/ac_user_guide/develop/dotnet/msg_modules/create_msg_module.html) for more details on creating message modules.

## Deployment

To publish the message module to ScaleOut Active Caching, right-click on the Flight project in Visual Studio's solution explorer and select "Publish". The publishing window will allow you to select from several predefined targets (Windows vs. Linux). Click the "Publish" button to create a zipped deployment package.

Use the [ScaleOut Active Caching UI](https://static.scaleoutsoftware.com/docs/ac_user_guide/ui/modules.html) to upload the package and manage your deployment. Once deployed, the package will run as a worker process on every host in the ScaleOut StateServer cluster.

## Load Generation

A *SampleClient* project is also included to send a message to the Flight message module. This project demonstrates how use the ScaleOut Active Caching NuGet package (Scaleout.Modules.Client) and its `MessageModuleClient` class.