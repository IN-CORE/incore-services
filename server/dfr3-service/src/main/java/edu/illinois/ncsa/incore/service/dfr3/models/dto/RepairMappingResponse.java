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
import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;

import java.util.HashMap;
import java.util.Map;

public class RepairMappingResponse {
    @JsonProperty("sets")
    public Map<String, RepairSet> repairSets = new HashMap<>();

    @JsonProperty("mapping")
    public Map<String, String> repairToInventoryMapping = new HashMap<>();

    public RepairMappingResponse() {

    }

    public RepairMappingResponse(Map<String, RepairSet> repairSets, Map<String, String> repairMap) {
        this.repairSets = repairSets;
        this.repairToInventoryMapping = repairMap;
    }

    public Map<String, RepairSet> getRepairSets() {
        return repairSets;
    }

    public Map<String, String> getRepairToInventoryMapping() {
        return repairToInventoryMapping;
    }
}
