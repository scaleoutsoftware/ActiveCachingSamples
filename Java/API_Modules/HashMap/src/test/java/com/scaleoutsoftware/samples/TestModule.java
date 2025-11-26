/*
 * (C) Copyright 2025 by ScaleOut Software, Inc.
 *
 * LICENSE AND DISCLAIMER
 * ----------------------
 * This material contains sample programming source code ("Sample Code").
 * ScaleOut Software, Inc. (SSI) grants you a nonexclusive license to compile,
 * link, run, display, reproduce, and prepare derivative works of
 * this Sample Code.  The Sample Code has not been thoroughly
 * tested under all conditions.  SSI, therefore, does not guarantee
 * or imply its reliability, serviceability, or function. SSI
 * provides no support services for the Sample Code.
 *
 * All Sample Code contained herein is provided to you "AS IS" without
 * any warranties of any kind. THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGMENT ARE EXPRESSLY
 * DISCLAIMED.  SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED
 * WARRANTIES, SO THE ABOVE EXCLUSIONS MAY NOT APPLY TO YOU.  IN NO
 * EVENT WILL SSI BE LIABLE TO ANY PARTY FOR ANY DIRECT, INDIRECT,
 * SPECIAL OR OTHER CONSEQUENTIAL DAMAGES FOR ANY USE OF THE SAMPLE CODE
 * INCLUDING, WITHOUT LIMITATION, ANY LOST PROFITS, BUSINESS
 * INTERRUPTION, LOSS OF PROGRAMS OR OTHER DATA ON YOUR INFORMATION
 * HANDLING SYSTEM OR OTHERWISE, EVEN IF WE ARE EXPRESSLY ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.scaleoutsoftware.samples;

import com.scaleoutsoftware.modules.hosting.ModulePackage;
import com.scaleoutsoftware.modules.hosting.ApiModuleOptions;
import com.scaleoutsoftware.modules.hosting.ApiModuleOptionsBuilder;
import com.scaleoutsoftware.modules.hosting.ModuleRegistrationException;
import com.scaleoutsoftware.modules.common.Constants;
import com.scaleout.client.GridConnection;

import com.scaleoutsoftware.samples.client.ExampleClient;
import com.scaleoutsoftware.samples.server.SossHashMap;
import com.scaleoutsoftware.samples.server.SossHashMapApiProcessor;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for module development.
 */
public class TestModule
{

    /**
     * Example unit test demonstrating invoking a "get" and "put" operation.
     */
    @Test
    public void testModuleLocally() {
        try {
            // instantiate the module package
            ModulePackage modulePackage = new ModulePackage();
            // define the ApiModuleOptions
            ApiModuleOptions<SossHashMap> apiModuleOptions = new ApiModuleOptionsBuilder<SossHashMap>(SossHashMap.class).build();
            // add the API module to the package
            modulePackage.addApiModule("SossHashMap", new SossHashMapApiProcessor(), apiModuleOptions);
            // run a local development package
            modulePackage.runLocalDevelopmentEnvironment();

            String expected = "World";
            // instantiate the example client
            ExampleClient exampleClient = new ExampleClient(GridConnection.connect(Constants.DEVELOPMENT_CONNECTION_STRING), "SossHashMap");
            // invoke the put operation -- this will invoke the ApiModule's "put" operation handler if it exists.
            boolean putSuccess = exampleClient.put("Hello", expected);
            // check for success
            Assert.assertTrue(putSuccess);
            // invoke the get operation -- this will invoke the ApiModule's "get" operation handler if it exists
            String value = exampleClient.get("Hello");
            // check for success
            Assert.assertEquals(expected, value);
        } catch (ModuleRegistrationException e) {
            Assert.fail(e.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
