/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.common.AllocationConstants;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity("UserFinalQuota")
public class UserFinalQuota {
    @Id
    @Property("_id")
    private ObjectId id;

    private String username;

    @JsonProperty("applicationLimits")
    private UserUsages applicationLimits;

    public UserFinalQuota() {
        this.applicationLimits = new UserUsages();
        this.username = null;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getUsername() { return this.username; }

    public UserUsages getApplicationLimits() { return this.applicationLimits; }

    public void setApplicationLimits(UserUsages applicationLimits) { this.applicationLimits = applicationLimits; }

}
