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

import com.scaleoutsoftware.modules.abstractions.MessageProcessor;
import com.scaleoutsoftware.modules.abstractions.MsgProcessingContext;
import com.scaleoutsoftware.modules.abstractions.ProcessingResult;
import com.scaleoutsoftware.modules.abstractions.NewObjectPolicy;
import com.scaleoutsoftware.modules.abstractions.ExpirationType;
import com.scaleoutsoftware.modules.abstractions.AlertSeverity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.io.PrintWriter;
import java.io.StringWriter;

public class EventProcessor extends MessageProcessor<GeoSpatialEventTracker> {

    @Override
    public ProcessingResult processMessage(MsgProcessingContext<GeoSpatialEventTracker> MsgProcessingContext, GeoSpatialEventTracker sossObject, byte[] message) {
        try {
            Logger LOGGER = LogManager.getLogger(EventProcessor.class.getName());
            // deserialize the message
            GeoSpatialMessage msg = GeoSpatialMessage.fromBytes(message);
            // this is an initialization message so we set our status and return.
            if(msg.initMessage()) {
                sossObject.setNodeType(msg.getNodeType());
                sossObject.setNodeCondition(Constants.NODE_CONDITION_NORMAL);
                sossObject.setRegion(msg.getRegion(), msg.getLongitude(), msg.getLatitude());
                return ProcessingResult.DoUpdate;
            }

            /* Run through the event processor rules. */
            // incoming message indicates the status tracker is offline or in normal operation.
            if(msg.offline() || msg.normalOperation()) {
                // set the state object statistics
                if(sossObject.experiencingMinorEvent() || sossObject.experiencingModerateEvent() ) {
                    sossObject.incrementFalseAlarmCount();
                    sossObject.incrementResolvedIncidents();
                }
                else if(sossObject.experiencingSevereEvent()) {
                    sossObject.incrementResolvedIncidents();
                }

                // set the node's alert level to normal, and update the state objects condition
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_NORMAL_ALERTLEVEL, Constants.CONTROLLER_NORMAL_ALERTLEVEL);
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a minor incident
            else if(msg.minorIncident()) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_MINOR_ALERTLEVEL, Constants.CONTROLLER_MINOR_ALERT_LEVEL);
                sossObject.incrementMinorEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a severe incident
            else if(msg.severeIncident()) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_SEVERE_ALERTLEVEL, Constants.CONTROLLER_SEVERE_ALERTLEVEL);
                sossObject.incrementSevereEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a moderate incident and this tracker has seen severe incidents
            else if(msg.moderateIncident() && sossObject.getSevereIncidentCount() > 0) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_MODERATE_ALERTLEVEL, Constants.CONTROLLER_MODERATE_ALERTLEVEL+2);
                sossObject.incrementModerateEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a moderate incident and this tracker has never had a severe incident and
            // this tracker has never seen a false incident
            else if(msg.moderateIncident() &&
                    sossObject.getSevereIncidentCount() == 0 &&
                    sossObject.getFalseIncidentCount() == 0) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_MODERATE_ALERTLEVEL+1, Constants.CONTROLLER_MODERATE_ALERTLEVEL+3);
                sossObject.incrementModerateEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a moderate incident and this tracker has never had a severe incident while the
            // heuristic of a false incident is greater than 50%
            else if(msg.moderateIncident() &&
                    sossObject.getSevereIncidentCount() == 0 &&
                    sossObject.getModerateIncidentCount() > 0 &&
                    ((double)(sossObject.getFalseIncidentCount()/sossObject.getModerateIncidentCount()) >= 0.5)) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_MODERATE_ALERTLEVEL+2, Constants.CONTROLLER_MODERATE_ALERTLEVEL+4);
                sossObject.incrementModerateEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates a moderate incident and this tracker has never had a severe incident while the
            // heuristic of a false incident is less than 50%
            else if(msg.moderateIncident() &&
                    sossObject.getSevereIncidentCount() == 0 &&
                    sossObject.getModerateIncidentCount() > 0 &&
                    ((double)(sossObject.getFalseIncidentCount()/sossObject.getModerateIncidentCount()) < 0.5)) {
                sossObject.setAlertLevel(Constants.INFRASTRUCTURE_MODERATE_ALERTLEVEL+3, Constants.CONTROLLER_MODERATE_ALERTLEVEL+5);
                sossObject.incrementModerateEventCount();
                sossObject.setNodeCondition(msg.getNodeCondition());
            }

            // the message indicates some form of incident -- update total incidents and add message to message list
            if(msg.minorIncident() || msg.moderateIncident() || msg.severeIncident()) {
                sossObject.incrementTotalIncidents();
                sossObject.addToIncidentList(msg);
            }
        } catch (Exception e) {
            // Catch all exceptions and send an alert using the UI alerter.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            MsgProcessingContext.sendUiAlert(AlertSeverity.Error, "Exception thrown by id" + MsgProcessingContext.getObjectId() + " " + pw.toString());
        }
        // Return ProcessingResult.DoUpdate if this method modified the SOSS object.
        // If no changes occurred or the changes are to be discarded, return ProcessingResult.NoUpdate.
        // To remove the SOSS object, return ProcessingResult.Remove;
        return ProcessingResult.DoUpdate;
    }

    /**
    * Instantiate a new instance of EventTracker.
    */
    @Override
    public GeoSpatialEventTracker createObject(String moduleName, String id) {
        return new GeoSpatialEventTracker(id);
    }
    
    /**
    * Generate a new object policy when a SOSS object is created. 
    */ 
    @Override
    public NewObjectPolicy getNewObjectPolicy(String moduleName, String id, GeoSpatialEventTracker object) {
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