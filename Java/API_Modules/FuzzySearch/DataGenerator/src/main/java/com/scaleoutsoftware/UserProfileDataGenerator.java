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

import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UserProfileDataGenerator {

    private static final ObjectMapper _objectMapper = new ObjectMapper();
    private static long _randomSeed = 3904876984576L;

    /*
     * Percentage of profiles where each property is populated.
     */
    private static double _idFrequency = 1.00;

    private static double _firstNameFrequency = 1.00;
    private static double _middleNameFrequency = 0.30;
    private static double _lastNameFrequency = 0.50;

    private static double _aliasesFrequency = 0.05;

    private static double _dateOfBirthFrequency = 1.00;

    private static double _streetAddressFrequency = 0.25;
    private static double _cityFrequency = 0.25;
    private static double _stateFrequency = 0.25;
    private static double _postalCodeFrequency = 0.50;

    private static double _phoneNumberFrequency = 0.85;
    private static double _emailAddressFrequency = 0.75;

    private static double _occupationFrequency = 0.50;
    private static double _organizationFrequency = 0.50;

    private static double _tagsFrequency = 0.02;
    private static double _notesFrequency = 0.02;

    private static final List<String> _tagValues =
            Arrays.asList(
                    "customer",
                    "employee",
                    "contractor",
                    "vendor",
                    "member",
                    "prospect",
                    "inactive",
                    "priority",
                    "verified",
                    "review"
            );

    public void generateData(int numProfiles, File outputFile) {

        if (numProfiles < 0) throw new IllegalArgumentException("numProfiles cannot be negative.");

        if (outputFile == null) throw new IllegalArgumentException("outputFile cannot be null.");

        Random random = new Random(_randomSeed);
        Faker faker = new Faker(random);

        File parent = outputFile.getParentFile();

        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IllegalArgumentException("Unable to create output directory: "+ parent);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            for (int i = 0; i < numProfiles; i++) {
                UserProfile profile = generateProfile(i + 1, faker, random);
                writer.write(_objectMapper.writeValueAsString(profile));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Unable to generate UserProfile data.", e);
        }
    }

    private UserProfile generateProfile(int profileNumber, Faker faker, Random random) {
        UserProfile profile = new UserProfile();
        if (include(random, _idFrequency)) {
            profile.setId(String.format("USER-%07d", profileNumber));
        }

        if (include(random, _firstNameFrequency)) {
            profile.setFirstName(faker.name().firstName());
        }

        if (include(random, _middleNameFrequency)) {
            profile.setMiddleName(faker.name().firstName());
        }

        if (include(random, _lastNameFrequency)) {
            profile.setLastName(faker.name().lastName());
        }

        if (include(random, _aliasesFrequency)) {
            profile.setAliases(generateAliases(faker, random));
        }

        if (include(random, _dateOfBirthFrequency)) {
            profile.setDateOfBirth(faker.timeAndDate().birthday(18,90,"yyyy-MM-dd"));
        }

        if (include(random, _streetAddressFrequency)) {
            profile.setStreetAddress(faker.address().streetAddress());
        }

        if (include(random, _cityFrequency)) {
            profile.setCity(faker.address().city());
        }

        if (include(random, _stateFrequency)) {
            profile.setState(faker.address().state());
        }

        if (include(random, _postalCodeFrequency)) {
            profile.setPostalCode(faker.address().zipCode());
        }

        if (include(random, _phoneNumberFrequency)) {
            profile.setPhoneNumber(faker.phoneNumber().phoneNumber());
        }

        if (include(random, _emailAddressFrequency)) {
            profile.setEmailAddress(faker.internet().emailAddress());
        }

        if (include(random, _occupationFrequency)) {
            profile.setOccupation(faker.job().title());
        }

        if (include(random, _organizationFrequency)) {
            profile.setOrganization(faker.company().name());
        }

        if (include(random, _tagsFrequency)) {
            profile.setTags(generateTags(random));
        }

        if (include(random, _notesFrequency)) {
            profile.setNotes(faker.lorem().sentence());
        }

        return profile;
    }

    private List<String> generateAliases(Faker faker, Random random) {

        int aliasCount = 1 + random.nextInt(3);

        List<String> aliases = new ArrayList<>(aliasCount);

        for (int i = 0; i < aliasCount; i++) {
            if(random.nextBoolean()) {
                aliases.add(faker.animal().name());
            } else {
                aliases.add(faker.planet().name());
            }

        }

        return aliases;
    }

    private List<String> generateTags(Random random) {

        int tagCount = 1 + random.nextInt(3);

        List<String> tags = new ArrayList<>(tagCount);

        while (tags.size() < tagCount) {
            String tag = _tagValues.get(random.nextInt(_tagValues.size()));

            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }

        return tags;
    }

    private boolean include(Random random, double frequency) {
        return random.nextDouble() < frequency;
    }
}