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
package com.scaleoutsoftware.client;

import com.scaleout.client.caching.DeserializationException;
import com.scaleoutsoftware.modules.client.ApiModuleClient;
import com.scaleoutsoftware.modules.client.ApiModuleException;
import com.scaleout.client.GridConnection;
import com.scaleoutsoftware.server.KryoSerializationHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class ProfileClient extends ApiModuleClient {

    public ProfileClient(GridConnection connection, String moduleName) {
        super(connection, moduleName);
    }

    public void updateUserProfile(String id, String jsonPatch) throws ApiModuleException {
        invoke(id, "updateUserProfile", jsonPatch.getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(10));
    }


    /**
     * Fuzzy search all of the UserProfile objects for the properties specified by
     * the parameter json patch document.
     * @param jsonPatchQuery the properties to fuzzy search for.
     * @return a list of strings where each returned string is an ID of a UserProfile.
     * @throws ApiModuleException if the ApiModule throws an exception.
     * @throws DeserializationException if there is an error serializing or deserialzing the result object.
     */
    public List<String> fuzzySearchAll(String jsonPatchQuery) throws ApiModuleException, DeserializationException {
        byte[] ret = invokeAll( "fuzzySearch", jsonPatchQuery.getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(15));
        return KryoSerializationHelper.deserializeList(ret);
    }

}