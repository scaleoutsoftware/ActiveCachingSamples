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
package com.scaleoutsoftware.samples.server;

import com.scaleoutsoftware.modules.abstractions.ApiProcessor;
import com.scaleoutsoftware.modules.abstractions.ApiProcessingContext;
import com.scaleoutsoftware.modules.abstractions.ProcessingResult;
import com.scaleoutsoftware.modules.abstractions.NewObjectPolicy;
import com.scaleoutsoftware.modules.abstractions.ExpirationType;
import com.scaleoutsoftware.modules.abstractions.ApiProcessorLockingMode;
import com.scaleoutsoftware.modules.abstractions.AlertSeverity;
import com.scaleoutsoftware.modules.abstractions.SossApiMethod;
import com.scaleoutsoftware.modules.abstractions.InvokeResult;
import com.scaleoutsoftware.modules.abstractions.ObjNotFoundBehavior;

import java.time.Duration;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
public class SossHashMapApiProcessor extends ApiProcessor<SossHashMap> {

    /**
     * Example "get" operation. Invoke through ApiModuleClient.invoke("objectId", "get", byte[] payloadKey).
     * See example in: ExampleClient.get(String key)
     * @return A UTF-8 String key or a UTF-8 String with contents "Not Found".
     */
    @SossApiMethod(operationId = "get", lockingMode = ApiProcessorLockingMode.None, objNotFoundBehavior = ObjNotFoundBehavior.Create)
    public InvokeResult getHandler(ApiProcessingContext<SossHashMap> processingContext, SossHashMap myObject, byte[] payload) {
        String key = new String(payload, StandardCharsets.UTF_8);
        String value = myObject.get(key);
        if(value != null) {
            return new InvokeResult() {
                @Override
                public byte[] getResult() {
                    return value.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public ProcessingResult getProcessingResult() {
                    return ProcessingResult.NoUpdate;
                }
            };
        } else {
            return new InvokeResult() {
                @Override
                public byte[] getResult() {
                    return "Not Found".getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public ProcessingResult getProcessingResult() {
                    return ProcessingResult.NoUpdate;
                }
            };
        }
    }

    /**
     * Example "put" operation. Invoke through ApiModuleClient.invoke("objectId", "get", byte[] payloadKeyValue).
     * See example in: ExampleClient.put(String key, String value)
     * @return A single byte 0x01 if the put operation was successfull, 0x00 if the put operation failed.
     */
    @SossApiMethod(operationId = "put", lockingMode = ApiProcessorLockingMode.ExclusiveLock, objNotFoundBehavior = ObjNotFoundBehavior.Create)
    public InvokeResult putHandler(ApiProcessingContext<SossHashMap> processingContext, SossHashMap myObject, byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        int keyLen = buffer.getInt();
        byte[] keyBytes = new byte[keyLen];
        buffer.get(keyBytes, 0, keyLen);
        int valLen = buffer.getInt();
        byte[] valBytes = new byte[valLen];
        buffer.get(valBytes, 0, valLen);
        myObject.put(new String(keyBytes, StandardCharsets.UTF_8), new String(valBytes, StandardCharsets.UTF_8));
        return new InvokeResult() {
            @Override
            public byte[] getResult() {
                return new byte[]{0x01};
            }

            @Override
            public ProcessingResult getProcessingResult() {
                return ProcessingResult.DoUpdate;
            }
        };
    }

    /**
    * Instantiate a new instance of SossHashMap.
    */
    @Override
    public SossHashMap createObject(String moduleName, String id) {
        return new SossHashMap(id);
    }
    
    /**
    * Generate a new object policy when a SOSS object is created. 
    */ 
    @Override
    public NewObjectPolicy getNewObjectPolicy(String moduleName, String id, SossHashMap object) {
        // return a NewObjectPolicy with an infinite timeout
        return new NewObjectPolicy() {
            @Override
            public Duration getExpirationDuration() {
                return Duration.ZERO;
            }

            @Override
            public ExpirationType getExpirationType() {
                return ExpirationType.Absolute;
            }
        };
    }
}