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

import com.scaleout.client.caching.DeserializationException;
import com.scaleout.client.caching.SerializationException;
import com.scaleoutsoftware.modules.abstractions.*;

import java.util.LinkedList;
import java.util.List;


public class SearchUserProfilesOperation extends ReduceApiProcessor<UserProfile, List<String>> {
    @Override
    public List<String> accumulatorFactory() {
        return new LinkedList<>();
    }

    @Override
    @SossEvalMethod(lockingMode = ApiProcessorLockingMode.None)
    public EvalResult<List<String>> evaluate(ApiProcessingContext<UserProfile> apiProcessingContext, List<String> accumulator, UserProfile profile, byte[] searchOptions) {
        // use jaro-winkler similarity to check search options for a match within this UserProfile
        // if it matches, add to the accumulator and return an EvalResult with the accumulator marking the result
        // to not update the object since we did not mutate the object.
        // If Id, postalCode, or phone number is supplied the search does exact matching.
        if(UserProfileSearcher.fuzzySearch(profile, searchOptions)) {
            accumulator.add(profile.getId());
        }
        return new EvalResult<>(accumulator, ProcessingResult.NoUpdate);
    }

    @Override
    public List<String> reduce(List<String> r1, List<String> r2) {
        // merge the results together
        r1.addAll(r2);
        return r1;
    }

    @Override
    public List<String> deserializeResult(byte[] bytes) {
        try {
            // fuzzy matching has a chance to accidentally return a large result object (list of matching user IDs)
            // the Kryo serialization library has very fast serialization/deserialization for large lists
            return KryoSerializationHelper.deserializeList(bytes);
        } catch (DeserializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serializeResult(List<String> strings) {
        try {
            return KryoSerializationHelper.serializeList(strings);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
