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
package com.scaleoutsoftware.samples.client;

import com.scaleoutsoftware.modules.client.ApiModuleClient;
import com.scaleoutsoftware.modules.client.ApiModuleException;
import com.scaleout.client.GridConnection;

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class ExampleClient extends ApiModuleClient {

    public ExampleClient(GridConnection connection, String moduleName) {
        super(connection, moduleName);
    }

    /**
     * Invoke the "get" operation ID on a SOSS object with the ID "ExampleObjectId"
     * @param key the key to find in the SOSS object
     * @return the value associated with "key", or "Not Found" in the SOSS object.
     * @throws ApiModuleException if the handler does not exist, the object did not exist and the CreateResult operation
     * threw an exception, or the handler threw an unhandled exception.
     */
    public String get(String key) throws ApiModuleException {
        byte[] result = invoke("ExampleObjectId", "get", key.getBytes(StandardCharsets.UTF_8));
        if(result != null)
            return new String(result, StandardCharsets.UTF_8);
        else
            return "Not Found";
    }

    /**
     * Invoke the "put" operation ID on a SOSS object with the ID "ExampleObjectId"
     * @param key the key to put in the SOSS object
     * @param value the value to associate with "key" in the SOSS object
     * @return the value associated with "key", or "Not Found" in the SOSS object.
     * @throws ApiModuleException if the handler does not exist, the object did not exist and the CreateResult operation
     * threw an exception, or the handler threw an unhandled exception.
     */
    public boolean put(String key, String value) throws ApiModuleException {
        // define a simple "put" wire protocol
        // int keyLen, UTF-8 string[keyLen]
        // int valLen, UTF-8 string[valLen]
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(keyBytes.length + valBytes.length + 8);
        buffer.putInt(keyBytes.length);
        buffer.put(keyBytes);
        buffer.putInt(valBytes.length);
        buffer.put(valBytes);
        byte[] result = invoke("ExampleObjectId", "put", buffer.array());
        return result[0] == 0x01;
    }

}