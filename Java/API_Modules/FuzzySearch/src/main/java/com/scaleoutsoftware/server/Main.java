/*
 * (C) Copyright 2026 by ScaleOut Software, Inc.
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
package com.scaleoutsoftware.server;

import com.scaleoutsoftware.modules.hosting.*;

import java.util.List;

/**
 * An API module's entry point. Run "mvn package" to build the deployable ZIP package.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // instantiate the module package
            ModulePackage modulePackage = new ModulePackage();
            // define the ApiModuleOptions
            ApiModuleOptions<UserProfile> apiModuleOptions = new ApiModuleOptionsBuilder<UserProfile>(UserProfile.class).build();
            // add the API module to the package
            ApiModule<UserProfile> apiModule = modulePackage.addApiModule("UserProfile", new UserProfileApiProcessor(), apiModuleOptions);
            // add the fuzzy search reduce operation to the API module
            apiModule.addReduceOperation("fuzzySearch", new SearchUserProfilesOperation(), new ParallelOperationOptionsBuilder<UserProfile, List<String>>(UserProfile.class).build());
            // wait for events
            modulePackage.waitForEvents();
        } catch (ModuleRegistrationException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}