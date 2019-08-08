/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;

import java.util.HashMap;
import java.util.Map;

public class RestorationMappingResponse {
    @JsonProperty("sets")
    public Map<String, RestorationSet> restorationSets = new HashMap<>();

    @JsonProperty("mapping")
    public Map<String, String> restorationToInventoryMapping = new HashMap<>();

    public RestorationMappingResponse() {

    }

    public RestorationMappingResponse(Map<String, RestorationSet> restorationSets, Map<String, String> restorationMap) {
        this.restorationSets = restorationSets;
        this.restorationToInventoryMapping = restorationMap;
    }

    public Map<String, RestorationSet> getRestorationSets() {
        return restorationSets;
    }

    public Map<String, String> getRestorationToInventoryMapping() {
        return restorationToInventoryMapping;
    }
}
