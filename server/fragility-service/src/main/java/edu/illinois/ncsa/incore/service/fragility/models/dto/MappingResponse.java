/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;

import java.util.HashMap;
import java.util.Map;

public class MappingResponse {
    @JsonProperty("sets")
    public Map<String, FragilitySet> fragilitySets = new HashMap<>();

    @JsonProperty("mapping")
    public Map<String, String> fragilityToInventoryMapping = new HashMap<>();

    public MappingResponse() {

    }

    public MappingResponse(Map<String, FragilitySet> fragilitySets, Map<String, String> fragilityMap) {
        this.fragilitySets = fragilitySets;
        this.fragilityToInventoryMapping = fragilityMap;
    }

    public Map<String, FragilitySet> getFragilitySets() {
        return fragilitySets;
    }

    public Map<String, String> getFragilityToInventoryMapping() {
        return fragilityToInventoryMapping;
    }
}
