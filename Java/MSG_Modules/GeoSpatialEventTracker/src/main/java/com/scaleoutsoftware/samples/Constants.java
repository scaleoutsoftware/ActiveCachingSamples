/*
 * (C) Copyright 2024 by ScaleOut Software, Inc.
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

public class Constants {
    /* CSV processing */
    final static String[] HEADERS = new String[] {"Id","nodeType","status","region","longitude","latitude","attacked"};
    final static int HEADER_IDX_ID        = 0;
    final static int HEADER_IDX_TYPE      = 1;
    final static int HEADER_IDX_STATUS    = 2;
    final static int HEADER_IDX_REGION    = 3;
    final static int HEADER_IDX_LONGITUDE = 4;
    final static int HEADER_IDX_LATITUDE  = 5;
    final static int HEADER_IDX_ATTACKED  = 6;

    final static String TARGET_MODULE_NAME = "GeoSpatialEventTracker";

    final static String NODE_TYPE_INFRASTRUCTURE   = "infrastructure";
    final static String NODE_TYPE_CONTROLLER       = "controller";

    final static long ATTACK_DURATION = 120000;

    /* Power grid node status */
    final static String MESSAGE_TYPE_INIT          = "init";       // initialization message
    final static String MESSAGE_TYPE_STATUS        = "status";     // status message
    final static String NODE_CONDITION_OFFLINE     = "offline";    // node was powered off
    final static String NODE_CONDITION_NORMAL      = "normal";     // base condition
    final static String NODE_CONDITION_MINOR       = "minor";      // previously abnormal
    final static String NODE_CONDITION_MODERATE    = "moderate";   // previously suspect attack
    final static String NODE_CONDITION_SEVERE      = "severe";     // previously attack

    /* CSV data uses colors instead of node condition */
    public static String convertColorToStatus(String color) {
        switch(color) {
            case "blue":
                return Constants.NODE_CONDITION_OFFLINE;
            case "green":
                return Constants.NODE_CONDITION_NORMAL;
            case "yellow":
                return Constants.NODE_CONDITION_MINOR;
            case "orange":
                return Constants.NODE_CONDITION_MODERATE;
            case "red":
                return Constants.NODE_CONDITION_SEVERE;
            default:
                throw new IllegalStateException("Unknown status: " + color);
        }
    }

    /* EventTracker location */
    final static String REGION_NW = "NW";
    final static String REGION_SW = "SW";
    final static String REGION_MN = "MN";
    final static String REGION_MS = "MS";
    final static String REGION_NE = "NE";
    final static String REGION_SE = "SE";
    final static long SIMULATION_DELAY_OFFLINE     = 20000L;
    final static long SIMULATION_DELAY_NORMAL      = 6000L;
    final static long SIMULATION_DELAY_MINOR       = 6000L;
    final static long SIMULATION_DELAY_MODERATE    = 10000L;
    final static long SIMULATION_DELAY_SEVERE      = 60000L;

    public static long findDelayTimeMs(String condition) {
        switch(condition) {
            case Constants.NODE_CONDITION_OFFLINE:
                return SIMULATION_DELAY_OFFLINE;
            case Constants.NODE_CONDITION_NORMAL:
                return SIMULATION_DELAY_NORMAL;
            case Constants.NODE_CONDITION_MINOR:
                return SIMULATION_DELAY_MINOR;
            case Constants.NODE_CONDITION_MODERATE:
                return SIMULATION_DELAY_MODERATE;
            case Constants.NODE_CONDITION_SEVERE:
                return SIMULATION_DELAY_SEVERE;
            default:
                throw new IllegalStateException("Unknown status: " + condition);
        }
    }

    final static int SIMULATION_PR_OFFLINE	    = 5;
    final static int SIMULATION_PR_NORMAL	    = 80;
    final static int SIMULATION_PR_MINOR	    = 20;
    final static int SIMULATION_PR_MODERATE     = 5;
    final static int SIMULATION_PR_SEVERE		= 1;

    /* Alert Levels for Controller and Infrastructure nodes. */
    // controller and infrastructure normal alert levels
    final static int CONTROLLER_NORMAL_ALERTLEVEL          = 10;
    final static int INFRASTRUCTURE_NORMAL_ALERTLEVEL      = 10;

    // controller and infrastructure minor alert levels
    final static int CONTROLLER_MINOR_ALERT_LEVEL          = 30;
    final static int INFRASTRUCTURE_MINOR_ALERTLEVEL       = 30;

    // controller and infrastructure moderate alert levels
    final static int CONTROLLER_MODERATE_ALERTLEVEL        = 40;
    final static int INFRASTRUCTURE_MODERATE_ALERTLEVEL    = 40;

    // controller and infrastructure severe alert levels
    final static int CONTROLLER_SEVERE_ALERTLEVEL          = 100;
    final static int INFRASTRUCTURE_SEVERE_ALERTLEVEL      = 100;

    /* Prevent power grid state object memory growth */
    final static int MAX_INCIDENT_LIST_SIZE        = 15;
    final static int INCIDENT_LIST_SUBLIST_FROM    = 10;
    final static int INCIDENT_LIST_SUBLIST_TO      = MAX_INCIDENT_LIST_SIZE;

}
