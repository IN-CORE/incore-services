/*******************************************************************************
 * Copyright (c) 2023 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang
 */

package edu.illinois.ncsa.incore.service.dfr3.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;

@Entity("mappingEntryKeys")
public class MappingEntryKey {
    private String name;
    private String description;
    private Boolean defaultKey;

    @JsonProperty("config")
    private MappingEntryKeyConfig config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public Boolean getDefaultKey(){
        return defaultKey;
    }

    public void setDefaultKey(Boolean defaultKey){
        this.defaultKey = defaultKey;
    }

    public MappingEntryKeyConfig getConfig(){
        return config;
    }

    public void setConfig(MappingEntryKeyConfig config){
        this.config = config;
    }

}
