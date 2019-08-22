/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.dfr3.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.dfr3.models.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.service.dfr3.models.mapping.PropertyMatch;
import ncsa.tools.common.exceptions.ParseException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MappingSet {

    @Id
    @Property("_id")
    private ObjectId id;

    private String name;
    private String hazardType;
    private String inventoryType;

    @JsonProperty("mappingType")
    private String mappingType;

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

}
