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
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity("GroupAllocations")
public class GroupAllocations {
    @Id
    @Property("_id")
    private ObjectId id;

    private String groupname;

    @JsonProperty("limit")
    private UserUsages limits;

    public GroupAllocations() {
        this.limits = new UserUsages();
        this.groupname = null;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getGroupname() { return this.groupname; }

    public void setGroupname(String groupname) { this.groupname = groupname; }

    public UserUsages getLimits() { return this.limits; }

    public void setLimits(UserUsages limits) { this.limits = limits; }
}
