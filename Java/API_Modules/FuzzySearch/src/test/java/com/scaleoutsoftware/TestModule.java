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
package com.scaleoutsoftware;

import com.google.gson.Gson;
import com.scaleout.client.GridConnectException;
import com.scaleout.client.caching.Cache;
import com.scaleout.client.caching.CacheBuilder;
import com.scaleout.client.caching.CacheResponse;
import com.scaleout.client.caching.RequestStatus;
import com.scaleoutsoftware.modules.common.JsonDeserializer;
import com.scaleoutsoftware.modules.common.JsonSerializer;
import com.scaleoutsoftware.modules.hosting.*;
import com.scaleoutsoftware.modules.common.Constants;
import com.scaleout.client.GridConnection;

import com.scaleoutsoftware.client.ProfileClient;
import com.scaleoutsoftware.server.SearchUserProfilesOperation;
import com.scaleoutsoftware.server.UserProfile;
import com.scaleoutsoftware.server.UserProfileApiProcessor;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestModule
{
    @Test
    public void testModuleLocally() {
        try {
            // instantiate the module package
            ModulePackage modulePackage = new ModulePackage();
            // define the ApiModuleOptions
            ApiModuleOptions<UserProfile> apiModuleOptions = new ApiModuleOptionsBuilder<UserProfile>(UserProfile.class).build();
            // add the API module to the package
            ApiModule<UserProfile> module = modulePackage.addApiModule("UserProfile", new UserProfileApiProcessor(), apiModuleOptions);
            module.addReduceOperation("fuzzySearch", new SearchUserProfilesOperation(), new ParallelOperationOptionsBuilder<UserProfile, List<String>>(UserProfile.class).build());
            // run a local development package
            modulePackage.runLocalDevelopmentEnvironment();
            // connect to the local service
            GridConnection connection = GridConnection.connect(Constants.DEVELOPMENT_CONNECTION_STRING);
            // construct a cache for UserProfile objects, keyed by a string
            Cache<String,UserProfile> profileCache = new CacheBuilder<String,UserProfile>(connection, "UserProfile", String.class)
                    .customSerialization(new JsonSerializer<>(new Gson(), UserProfile.class), new JsonDeserializer<>(new Gson(), UserProfile.class))
                    .build();
            // hydrate the cache with user profile objects
            hydrateCache(profileCache);
            // construct a profile client to send operations to the UserProfile ApiModule
            ProfileClient profileClient = new ProfileClient(connection, "UserProfile");
            // create a JSON patch document with the properties to search for within each UserProfile
            String search =
                    "{"
                            + "\"firstName\":\"john\","
                            + "\"dateOfBirth\":\"1980-01-\","
                            + "\"alias\":\"goose\""
                            + "}";
            List<String> profileMatches = profileClient.fuzzySearchAll(search);
            System.out.println("found " + profileMatches.size() + " matches.");
            for(String id: profileMatches) {
                CacheResponse<String,UserProfile> response = profileCache.read(id);
                if(response.getStatus() == RequestStatus.ObjectRetrieved) {
                    System.out.println(response.getValue());
                }
            }
        } catch (ModuleRegistrationException e) {
            Assert.fail(e.getMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    void hydrateCache(Cache<String,UserProfile> profileCache) {
        // populate the cache from a file
        try {
            UserProfileHydrater hydrater = new UserProfileHydrater();
            File inputFile = new File("./profiles_1000.json");
            hydrater.hydrate(profileCache, inputFile, 8);
        } catch (GridConnectException e) {
            throw new RuntimeException(e);
        }

    }
}
