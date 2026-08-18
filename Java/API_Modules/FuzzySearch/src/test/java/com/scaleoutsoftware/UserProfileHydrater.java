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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaleout.client.GridConnectException;
import com.scaleout.client.caching.*;
import com.scaleoutsoftware.server.UserProfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserProfileHydrater {

    private static final ObjectMapper _objectMapper = new ObjectMapper();

    // Hydrates the parameter cache with JSON objects from the input file.
    public void hydrate(Cache<String,UserProfile> toHydrate, File inputFile, int numThreads) throws GridConnectException {
        if (inputFile == null) throw new IllegalArgumentException("inputFile cannot be null.");
        if (!inputFile.exists()) throw new IllegalArgumentException("inputFile does not exist: "+ inputFile.getAbsolutePath());
        if (numThreads <= 0) throw new IllegalArgumentException("numThreads must be greater than zero.");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<Integer>> futures = new ArrayList<>();

        int numStarted = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                UserProfile profile = _objectMapper.readValue(line, UserProfile.class);

                futures.add(executor.submit(() -> hydrateUserProfile(toHydrate, profile)));

                numStarted++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read UserProfile data.",e);
        } finally {
            executor.shutdown();
        }

        int numCompletions = 0;

        for (Future<Integer> future : futures) {
            try {
                numCompletions += future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for hydration.",e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("UserProfile hydration failed.", e.getCause());
            }
        }

        if (numStarted != numCompletions) {
            throw new IllegalStateException("Hydration count mismatch. Started="+ numStarted+ ", completed="+ numCompletions);
        }

        System.out.println("Hydration complete. Profiles=" + numCompletions);
    }

    private int hydrateUserProfile(Cache<String,UserProfile> cache, UserProfile profile) {
        try {
            CacheResponse<String,UserProfile> response = cache.addOrUpdate(profile.getId(), profile);
            if(response.getStatus() != RequestStatus.ObjectAdded && response.getStatus() != RequestStatus.ObjectUpdated) {
                System.err.println("Response " + response.getStatus());
                throw new IllegalStateException("Disaster, " + response.getStatus());
            }

        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }
}