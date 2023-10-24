/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.bson.types.ObjectId;

@XmlRootElement
@Entity("UserFinalQuota")
public class UserFinalQuota {
    @Id
    @Property("_id")
    private ObjectId id;

    private String username;

    @JsonProperty("applicableLimits")
    private UserUsages applicableLimits;

    public UserFinalQuota() {
        this.applicableLimits = new UserUsages();
        this.username = null;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getUsername() { return this.username; }

    public UserUsages getApplicableLimits() { return this.applicableLimits; }

    public void setApplicableLimits(UserUsages applicableLimits) { this.applicableLimits = applicableLimits; }

}
