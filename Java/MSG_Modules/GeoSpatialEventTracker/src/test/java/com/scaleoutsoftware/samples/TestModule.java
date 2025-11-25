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
import com.scaleoutsoftware.modules.hosting.MsgModuleOptions;
import com.scaleoutsoftware.modules.hosting.MsgModuleOptionsBuilder;
import com.scaleoutsoftware.modules.hosting.ModuleRegistrationException;
import com.scaleoutsoftware.modules.client.MsgModuleClient;
import com.scaleoutsoftware.modules.client.MsgModuleClientBuilder;
import com.scaleout.client.caching.Cache;
import com.scaleout.client.caching.CacheResponse;
import com.scaleout.client.caching.RequestStatus;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for module development.
 */
public class TestModule
{

    @Test
    public void testModuleLocally() {

        try {
            // instantiate the module package
            ModulePackage modulePackage = new ModulePackage();
            // define the MsgModuleOptions
            MsgModuleOptions<GeoSpatialEventTracker> msgModuleOptions = new MsgModuleOptionsBuilder<GeoSpatialEventTracker>(GeoSpatialEventTracker.class).build();
            // add the MSG module to the package
            modulePackage.addMsgModule("GeoSpatialEventTracker", new EventProcessor(), msgModuleOptions);
            // run a local development package
            modulePackage.runLocalDevelopmentEnvironment();
        } catch (ModuleRegistrationException e) {
            Assert.fail(e.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
