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

import com.scaleoutsoftware.modules.abstractions.ApiProcessor;
import com.scaleoutsoftware.modules.abstractions.ApiProcessingContext;
import com.scaleoutsoftware.modules.abstractions.ProcessingResult;
import com.scaleoutsoftware.modules.abstractions.NewObjectPolicy;
import com.scaleoutsoftware.modules.abstractions.ExpirationType;
import com.scaleoutsoftware.modules.abstractions.ApiProcessorLockingMode;
import com.scaleoutsoftware.modules.abstractions.SossApiMethod;
import com.scaleoutsoftware.modules.abstractions.InvokeResult;
import com.scaleoutsoftware.modules.abstractions.ObjNotFoundBehavior;

import java.time.Duration;

public class UserProfileApiProcessor extends ApiProcessor<UserProfile> {


    @SossApiMethod(operationId = "updateUserProfile", lockingMode = ApiProcessorLockingMode.ExclusiveLock, objNotFoundBehavior = ObjNotFoundBehavior.Create)
    public InvokeResult updateUserProfile(ApiProcessingContext<UserProfile> context, UserProfile profile, byte[] incomingUserProfileChanges) {
        try {
            // update the user profile based on the incoming patch
            UserProfileUpdater.updateUserProfile(profile, incomingUserProfileChanges);
            return new InvokeResult() {
                @Override
                public byte[] getResult() {
                    return new byte[] { 0x01};
                }

                @Override
                public ProcessingResult getProcessingResult() {
                    return ProcessingResult.DoUpdate;
                }
            };
        } catch (Exception e) {
            return new InvokeResult() {
                @Override
                public byte[] getResult() {
                    return new byte[] { 0x00};
                }

                @Override
                public ProcessingResult getProcessingResult() {
                    return ProcessingResult.NoUpdate;
                }
            };
        }
    }

    /**
    * Instantiate a new instance of UserDocument.
    */
    @Override
    public UserProfile createObject(String moduleName, String id) {
        UserProfile profile = new UserProfile();
        profile.setId(id);
        return profile;
    }
    
    /**
    * Generate a new object policy when a SOSS object is created. 
    */ 
    @Override
    public NewObjectPolicy getNewObjectPolicy(String moduleName, String id, UserProfile object) {
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