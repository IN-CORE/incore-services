/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInfo {
    private String sub;

    @JsonProperty("email_verified")
    private boolean emailVerified;
    private String name;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyNname;


    private String email;

    public String getSub() {
        return sub;
    }

    public boolean getEmailVerified() {
        return emailVerified;
    }

    public String getName() {
        return name;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyNname;
    }

    public String getEmail() {
        return email;
    }

}
