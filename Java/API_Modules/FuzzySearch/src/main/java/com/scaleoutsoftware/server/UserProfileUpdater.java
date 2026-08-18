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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserProfileUpdater {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<String> UPDATEABLE_FIELDS =
            new HashSet<>(Arrays.asList(
                    "firstName",
                    "middleName",
                    "lastName",
                    "aliases",
                    "dateOfBirth",
                    "streetAddress",
                    "city",
                    "state",
                    "postalCode",
                    "phoneNumber",
                    "emailAddress",
                    "occupation",
                    "organization",
                    "tags",
                    "notes"
            ));

    public static void updateUserProfile(UserProfile profile, byte[] incomingUserProfileChanges) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null.");

        if (incomingUserProfileChanges == null) return;

        final JsonNode patch;
        try {
            patch = OBJECT_MAPPER.readTree(incomingUserProfileChanges);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize UserProfile update.", e);
        }

        if (patch == null || !patch.isObject()) throw new IllegalArgumentException("UserProfile update must be a JSON object.");

        // applying the patch workflow...
        // 1) validate the incoming patch
        // 2) create copy of current profile
        // 3) apply the patch to the copy
        // 4) validate the copy
        // 5) swap copy and profile-to-update
        // ... this prevents bad patches from blowing up the original user profile
        validateFields(patch);

        UserProfile updatedProfile = copy(profile);

        applyPatch(updatedProfile, patch);

        validateProfile(updatedProfile);

        copyInto(updatedProfile, profile);
    }

    private static void applyPatch(UserProfile profile, JsonNode patch) {
        if (patch.has("firstName")) {
            profile.setFirstName(readString(patch, "firstName"));
        }

        if (patch.has("middleName")) {
            profile.setMiddleName(readString(patch, "middleName"));
        }

        if (patch.has("lastName")) {
            profile.setLastName(readString(patch, "lastName"));
        }

        if (patch.has("aliases")) {
            profile.setAliases(readStringList(patch, "aliases"));
        }

        if (patch.has("dateOfBirth")) {
            profile.setDateOfBirth(readString(patch, "dateOfBirth"));
        }

        if (patch.has("streetAddress")) {
            profile.setStreetAddress(readString(patch, "streetAddress"));
        }

        if (patch.has("city")) {
            profile.setCity(readString(patch, "city"));
        }

        if (patch.has("state")) {
            profile.setState(readString(patch, "state"));
        }

        if (patch.has("postalCode")) {
            profile.setPostalCode(readString(patch, "postalCode"));
        }

        if (patch.has("phoneNumber")) {
            profile.setPhoneNumber(readString(patch, "phoneNumber"));
        }

        if (patch.has("emailAddress")) {
            profile.setEmailAddress(readString(patch, "emailAddress"));
        }

        if (patch.has("occupation")) {
            profile.setOccupation(readString(patch, "occupation"));
        }

        if (patch.has("organization")) {
            profile.setOrganization(readString(patch, "organization"));
        }

        if (patch.has("tags")) {
            profile.setTags(readStringList(patch, "tags"));
        }

        if (patch.has("notes")) {
            profile.setNotes(readString(patch, "notes"));
        }
    }

    private static String readString(JsonNode patch, String field) {
        JsonNode value = patch.get(field);

        if (value == null || value.isNull()) {
            return "";
        }

        if (!value.isTextual()) throw new IllegalArgumentException( field + " must be a string.");

        return value.asText();
    }

    private static List<String> readStringList(JsonNode patch, String field) {
        JsonNode value = patch.get(field);

        if (value == null || value.isNull()) return new ArrayList<>();

        if (!value.isArray()) throw new IllegalArgumentException(field + " must be an array of strings.");

        List<String> result = new ArrayList<>();

        for (JsonNode item : value) {
            if (!item.isTextual()) throw new IllegalArgumentException(field + " must contain only strings.");

            result.add(item.asText());
        }

        return result;
    }

    private static void validateFields(JsonNode patch) {
        Iterator<String> fields = patch.fieldNames();

        while (fields.hasNext()) {
            String field = fields.next();

            if ("id".equals(field)) throw new IllegalArgumentException("id cannot be updated.");

            if (!UPDATEABLE_FIELDS.contains(field)) throw new IllegalArgumentException("Unknown UserProfile property: " + field);
        }
    }

    private static void validateProfile(UserProfile profile) {
        if (profile.getId() == null || profile.getId().isEmpty()) throw new IllegalArgumentException("Id cannot be empty.");
        if (profile.getFirstName() == null || profile.getFirstName().isEmpty()) throw new IllegalArgumentException("firstName cannot be empty.");
    }

    private static UserProfile copy(UserProfile source) {
        UserProfile copy = new UserProfile();
        copyInto(source, copy);
        return copy;
    }

    private static void copyInto(UserProfile source, UserProfile destination) {
        destination.setId(source.getId());
        destination.setFirstName(source.getFirstName());
        destination.setMiddleName(source.getMiddleName());
        destination.setLastName(source.getLastName());
        destination.setAliases(new ArrayList<>(source.getAliases()));
        destination.setDateOfBirth(source.getDateOfBirth());
        destination.setStreetAddress(source.getStreetAddress());
        destination.setCity(source.getCity());
        destination.setState(source.getState());
        destination.setPostalCode(source.getPostalCode());
        destination.setPhoneNumber(source.getPhoneNumber());
        destination.setEmailAddress(source.getEmailAddress());
        destination.setOccupation(source.getOccupation());
        destination.setOrganization(source.getOrganization());
        destination.setTags(new ArrayList<>(source.getTags()));
        destination.setNotes(source.getNotes());
    }
}