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
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UserProfileSearcher {

    private static final ObjectMapper _objectMapper = new ObjectMapper();

    private static final JaroWinklerSimilarity _similarity = new JaroWinklerSimilarity();

    private static final double _minimumSimilarity = 0.85d;

    private UserProfileSearcher() {
    }

    public static boolean fuzzySearch(UserProfile profile, byte[] searchOptions) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null.");
        if (searchOptions == null) throw new IllegalArgumentException("searchOptions cannot be null.");

        final JsonNode search;

        try {
            search = _objectMapper.readTree(searchOptions);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize search options.", e);
        }

        if (search == null || !search.isObject()) {
            throw new IllegalArgumentException("Search options must be a JSON object.");
        }

        if (search.isEmpty()) {
            throw new IllegalArgumentException("Search options cannot be empty.");
        }

        Iterator<Map.Entry<String, JsonNode>> fields = search.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

            String fieldName = field.getKey();
            String searchValue = readSearchValue(fieldName, field.getValue());

            if (!matches(profile, fieldName, searchValue)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matches(UserProfile profile, String field, String searchValue) {

        switch (field) {
            // Exact / normalized fields
            case "id":
                return exactMatch(
                        searchValue,
                        profile.getId());

            case "postalCode":
                return exactMatch(
                        searchValue,
                        profile.getPostalCode());

            case "phoneNumber":
                return phoneMatch(
                        searchValue,
                        profile.getPhoneNumber());

            // Fuzzy fields
            case "firstName":
                return fuzzyMatch(
                        searchValue,
                        profile.getFirstName());

            case "middleName":
                return fuzzyMatch(
                        searchValue,
                        profile.getMiddleName());

            case "lastName":
                return fuzzyMatch(
                        searchValue,
                        profile.getLastName());

            case "dateOfBirth":
                return fuzzyMatch(
                        searchValue,
                        profile.getDateOfBirth());

            case "emailAddress":
                return fuzzyMatch(
                        searchValue,
                        profile.getEmailAddress());

            case "streetAddress":
                return fuzzyMatch(
                        searchValue,
                        profile.getStreetAddress());

            case "city":
                return fuzzyMatch(
                        searchValue,
                        profile.getCity());

            case "state":
                return fuzzyMatch(
                        searchValue,
                        profile.getState());

            case "occupation":
                return fuzzyMatch(
                        searchValue,
                        profile.getOccupation());

            case "organization":
                return fuzzyMatch(
                        searchValue,
                        profile.getOrganization());

            case "notes":
                return fuzzyMatch(
                        searchValue,
                        profile.getNotes());

            case "aliases":
                return fuzzyMatchList(
                        searchValue,
                        profile.getAliases());

            case "alias":
                boolean match = false;
                for(String alias : profile.getAliases()) {
                    match = fuzzyMatch(searchValue, alias);
                    if (match) break;
                }
                return match;

            case "tags":
                return fuzzyMatchList(
                        searchValue,
                        profile.getTags());

            default:
                throw new IllegalArgumentException("Unknown UserProfile search property: " + field);
        }
    }

    private static boolean fuzzyMatch(String searchValue, String profileValue) {
        if (profileValue == null || profileValue.isEmpty()) {
            return false;
        }

        double score = _similarity.apply(normalize(searchValue), normalize(profileValue));

        return score >= _minimumSimilarity;
    }

    private static boolean fuzzyMatchList(String searchValue, List<String> profileValues) {
        if (profileValues == null || profileValues.isEmpty()) return false;

        for (String profileValue : profileValues) {
            if (fuzzyMatch(searchValue, profileValue)) {
                return true;
            }
        }

        return false;
    }

    private static boolean exactMatch(String searchValue, String profileValue) {
        if (profileValue == null) return false;

        return normalize(searchValue).equals(normalize(profileValue));
    }

    private static boolean phoneMatch(String searchValue, String profileValue) {
        if (profileValue == null) {
            return false;
        }

        return normalizePhone(searchValue).equals(normalizePhone(profileValue));
    }

    private static String readSearchValue(String field, JsonNode value) {
        if (value == null || value.isNull() || !value.isTextual()) throw new IllegalArgumentException(field + " must be a string.");

        String searchValue = value.asText().trim();

        if (searchValue.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }

        return searchValue;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String value) {
        return value.replaceAll("[^0-9]", "");
    }
}