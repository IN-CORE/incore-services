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
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@Entity("MappingSet")
public class MappingSet {

    @Id
    @Property("_id")
    private ObjectId id;

    private String name;
    private String hazardType;
    private String inventoryType;
    private List<String> dataTypes;

    @JsonProperty("mappingType")
    private String mappingType;

    private final List<Mapping> mappings = new ArrayList<>();
    private String creator;
    private String owner;

    private MappingEntryKey[] mappingEntryKeys;

    /**
     * spaces the object belongs to. Calculated at runtime.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    public String getId() {
        return (id == null) ? null : id.toString();
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

    public List<String> getDataTypes() { return dataTypes; }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public MappingEntryKey[] getMappingEntryKeys() {
        return mappingEntryKeys;
    }

    public void setRetrofitDefinitions(MappingEntryKey[] mappingEntryKeys) {
        this.mappingEntryKeys = mappingEntryKeys;
    }
}
