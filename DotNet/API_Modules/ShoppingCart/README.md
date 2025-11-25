# .NET Shopping Cart API Module Example

## Overview

This example demonstrates how to use [ScaleOut Active Caching](https://static.scaleoutsoftware.com/docs/ac_user_guide/intro/intro.html) to create a .NET API module representing a shopping cart.

The *ShoppingCart* project contains the API module implementation, where `ShoppingCartApiProcessor.cs` implements the module's core logic in methods marked with the `[ApiMethod]` attribute. See [Creating an API Module Project](https://static.scaleoutsoftware.com/docs/ac_user_guide/develop/dotnet/api_modules/create_api_module.html) for more details on creating API modules.

## Deployment

To publish the API module to ScaleOut Active Caching, right-click on the ShoppingCart project in Visual Studio's solution explorer and select "Publish". The publishing window will allow you to select from several predefined targets (Windows vs. Linux). Click the "Publish" button to create a zipped deployment package.

Use the [ScaleOut Active Caching UI](https://static.scaleoutsoftware.com/docs/ac_user_guide/ui/modules.html) to upload the package and manage your deployment. Once deployed, the package will run as a worker process on every host in the ScaleOut StateServer cluster.

## Load Generation

A *LoadGenerator* project is also included to simulate load on the Shopping Cart API module. This project demonstrates how to interact with the API module using the ScaleOut Active Caching client library. See the `ShoppingCartApiClient.cs` file for examples of how to invoke the API methods defined in a remote API module.