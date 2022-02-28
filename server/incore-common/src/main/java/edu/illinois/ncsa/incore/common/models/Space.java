/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
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
import edu.illinois.ncsa.incore.common.auth.PrivilegeLevel;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ywkim on 10/2/2017.
 */

@XmlRootElement
@Entity("Space")
public class Space {
    @Id
    @Property("_id")
    private ObjectId id;

    @JsonProperty("metadata")
    private SpaceMetadata metadata;

    private Privileges privileges;

    private List<String> members;

    @JsonProperty("usage")
    private SpaceUsage usage;

    public Space() {
        this.metadata = new SpaceMetadata("");
        this.members = new ArrayList<>();
        this.privileges = new Privileges();
        this.usage = new SpaceUsage();
    }

    public Space(String name) {
        this.metadata = new SpaceMetadata(name);
        this.members = new ArrayList<>();
        this.privileges = new Privileges();
        this.usage = new SpaceUsage();
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getName() {
        return this.metadata.getName();
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public void addPrivileges(Privileges privileges) {
        this.privileges.addUserPrivilegesMap(privileges.getUserPrivileges());
        this.privileges.addGroupPrivilegesMap(privileges.getGroupPrivileges());
    }

    public void addUserPrivileges(String username, PrivilegeLevel privilegeLevel) {
        this.privileges.addUserPrivileges(username, privilegeLevel);
    }

    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String id) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        if (id != null && !hasMember(id)) {
            members.add(id);
        }
    }

    public boolean hasMember(String id) {
        for (String datasetId : this.members) {
            if (datasetId.equals(id))
                return true;
        }
        return false;
    }

    public void removeMember(String id) {
        members.remove(id);
    }

    public void setMetadata(SpaceMetadata metadata) {
        this.metadata = metadata;
    }

    public SpaceMetadata getMetadata() {
        return this.metadata;
    }

    public PrivilegeLevel getUserPrivilegeLevel(String username) {
        return this.privileges.getUserPrivilegeLevel(username);
    }

    public PrivilegeLevel getGroupPrivilegeLevel(String username) {
        return this.privileges.getGroupPrivilegeLevel(username);
    }

    public SpaceUsage getUsage() { return this.usage; }

    public void setUsage(SpaceUsage usage) { this.usage = usage; }
}
