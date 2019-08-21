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

import java.util.HashMap;
import java.util.Map;

public class MappingResponse {
    @JsonProperty("sets")
    public Map<String, ?> dfr3Sets = new HashMap<>();

    @JsonProperty("mapping")
    public Map<String, String> dfr3SetsToInventoryMapping = new HashMap<>();

    public MappingResponse() {

    }

    public MappingResponse(Map<String, ?> setJsonMap, Map<String, String> setIdMap) {
        this.dfr3Sets = setJsonMap;
        this.dfr3SetsToInventoryMapping = setIdMap;
    }

    public Map<String, ?> getDfr3Sets() {
        return dfr3Sets;
    }

    public Map<String, String> getDfr3SetsToInventoryMapping() {
        return dfr3SetsToInventoryMapping;
    }
}
