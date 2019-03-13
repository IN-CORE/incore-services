/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.common.auth.PrivilegeLevel;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.data.models.spaces.Metadata;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ywkim on 10/2/2017.
 */

@XmlRootElement
public class Space {
    @Id
    @Property("_id")
    private ObjectId id = new ObjectId();

    @JsonProperty("metadata")
    private Metadata metadata;

    private Privileges privileges;

    private List<String> members;

    public Space(){
        this.metadata = new Metadata("");
        this.members = new ArrayList<>();
        this.privileges  = new Privileges();
    }

    public Space(String name){
        this.metadata = new Metadata(name);
        this.members = new ArrayList<>();
        this.privileges = new Privileges();
    }

    public String getId() {

        return id.toString();
    }

    public void setId(String id) {

        this.id = new ObjectId(id);
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

    public void addUserPrivileges(String username, PrivilegeLevel privilegeLevel){
        this.privileges.addUserPrivileges(username, privilegeLevel);
    }

    public List<String> getMembers() {
        return members;
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

    public boolean hasMember(String id){
        if(this.members == null) return false;
        for(String datasetId : this.members){
            if (datasetId.equals(id))
                return true;
        }
        return false;
    }

    public void removeMember(String id) {
        if (id != null) {
            getMembers().remove(id);
        }
    }

    public void setMetadata(Metadata metadata){
        this.metadata = metadata;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

}
