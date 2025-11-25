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

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class GeoSpatialMessage {
    // message properties
    private String type;
    private String id;
    private String node_condition;
    private String node_type;
    private String region;
    private double latitude;
    private double longitude;

    public GeoSpatialMessage() {

    }

    public GeoSpatialMessage(String nc) {
        type = Constants.MESSAGE_TYPE_STATUS;
        node_condition = nc;
    }

    public GeoSpatialMessage(String i, String r, String nc, String nt, double la, double lo) {
        type = Constants.MESSAGE_TYPE_INIT;
        id = i;
        region = r;
        node_condition = nc;
        node_type = nt;
        latitude = la;
        longitude = lo;
    }

    public boolean initMessage() {
        return Constants.MESSAGE_TYPE_INIT.compareTo(type) == 0;
    }

    public String getNodeType() {
        return node_type;
    }

    public String getNodeCondition() {
        return node_condition;
    }

    public String getRegion() {
        return region;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean offline() {
        return node_condition.compareTo(Constants.NODE_CONDITION_OFFLINE) == 0;
    }

    public boolean normalOperation() {
        return node_condition.compareTo(Constants.NODE_CONDITION_NORMAL) == 0;
    }

    public boolean minorIncident() {
        return node_condition.compareTo(Constants.NODE_CONDITION_MINOR) == 0;
    }

    public boolean severeIncident() {
        return node_condition.compareTo(Constants.NODE_CONDITION_SEVERE) == 0;
    }

    public boolean moderateIncident() {
        return node_condition.compareTo(Constants.NODE_CONDITION_MODERATE) == 0;
    }

    @Override
    public String toString() {
        return "GeoSpatialMessage{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", node_condition='" + node_condition + '\'' +
                ", node_type='" + node_type + '\'' +
                ", region='" + region + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public static byte[] toBytes(GeoSpatialMessage msg) {
        Gson gson = new Gson();
        return gson.toJson(msg).getBytes(StandardCharsets.UTF_8);
    }

    public static GeoSpatialMessage fromBytes(byte[] utf8Message) {
        Gson gson = new Gson();
        return gson.fromJson(new String(utf8Message, StandardCharsets.UTF_8), GeoSpatialMessage.class);
    }

}
