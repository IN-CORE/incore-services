/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.fragility.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.PropertyMatch;
import ncsa.tools.common.exceptions.ParseException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "mappingType")
@JsonSubTypes({@JsonSubTypes.Type(value = FragilityMappingSet.class, name = "fragilityMappingSet"),
    @JsonSubTypes.Type(value = RestorationMappingSet.class, name = "restorationMappingSet")})
@XmlRootElement
public abstract class MappingSet {
    @Id
    private ObjectId id;

    private String name;
    private String hazardType;
    private String inventoryType;
    private List<Mapping> mappings = new ArrayList<>();
    private Privileges privileges;
    private String creator;

    public String getId() {
        if (id == null) {
            return null;
        } else {
            return id.toHexString();
        }
    }

    public String getName() {
        return name;
    }

    public String getHazardType() {
        return hazardType;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public MatchFilterMap asMatchFilterMap() throws ParseException {
        List<PropertyMatch> propertyMatches = new ArrayList<>();

        for (Mapping mapping : mappings) {
            PropertyMatch propertyMatch = mapping.asPropertyMatch();
            propertyMatches.add(propertyMatch);
        }

        MatchFilterMap matchFilterMap = new MatchFilterMap(propertyMatches);

        return matchFilterMap;
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }
}
