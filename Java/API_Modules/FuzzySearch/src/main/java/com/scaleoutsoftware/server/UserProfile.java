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

import java.util.List;
import java.util.ArrayList;

public class UserProfile {
    // Id is a special property used by the Active Caching service to perform Queries.
    private String          Id               = "";
    private String          _firstName       = "";
    private String          _middleName      = "";
    private String          _lastName        = "";
    private List<String>    _aliases         = new ArrayList<>();
    private String          _dateOfBirth     = "";
    private String          _streetAddress   = "";
    private String          _city            = "";
    private String          _state           = "";
    private String          _postalCode      = "";
    private String          _phoneNumber     = "";
    private String          _emailAddress    = "";
    private String          _occupation      = "";
    private String          _organization    = "";
    private List<String>    _tags            = new ArrayList<>();
    private String          _notes           = "";

    public String getId() {
        return this.Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public String getMiddleName() {
        return _middleName;
    }

    public void setMiddleName(String middleName) {
        _middleName = middleName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        _lastName = lastName;
    }

    public List<String> getAliases() {
        return _aliases;
    }

    public void setAliases(List<String> aliases) {
        _aliases = aliases;
    }

    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        _dateOfBirth = dateOfBirth;
    }

    public String getStreetAddress() {
        return _streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        _streetAddress = streetAddress;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String getPostalCode() {
        return _postalCode;
    }

    public void setPostalCode(String postalCode) {
        _postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        _phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return _emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        _emailAddress = emailAddress;
    }

    public String getOccupation() {
        return _occupation;
    }

    public void setOccupation(String occupation) {
        _occupation = occupation;
    }

    public String getOrganization() {
        return _organization;
    }

    public void setOrganization(String organization) {
        _organization = organization;
    }

    public List<String> getTags() {
        return _tags;
    }

    public void setTags(List<String> tags) {
        _tags = tags;
    }

    public String getNotes() {
        return _notes;
    }

    public void setNotes(String notes) {
        _notes = notes;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "Id='" + Id + '\'' +
                ", _firstName='" + _firstName + '\'' +
                ", _middleName='" + _middleName + '\'' +
                ", _lastName='" + _lastName + '\'' +
                ", _aliases=" + _aliases +
                ", _dateOfBirth='" + _dateOfBirth + '\'' +
                ", _streetAddress='" + _streetAddress + '\'' +
                ", _city='" + _city + '\'' +
                ", _state='" + _state + '\'' +
                ", _postalCode='" + _postalCode + '\'' +
                ", _phoneNumber='" + _phoneNumber + '\'' +
                ", _emailAddress='" + _emailAddress + '\'' +
                ", _occupation='" + _occupation + '\'' +
                ", _organization='" + _organization + '\'' +
                ", _tags=" + _tags +
                ", _notes='" + _notes + '\'' +
                '}';
    }
}