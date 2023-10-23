/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.annotations.Embedded;
import org.apache.log4j.Logger;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


@Embedded
public class UserUsages {
    private static final Logger log = Logger.getLogger(UserUsages.class);

    @JsonProperty("total_number_of_datasets")
    public int datasets;

    @JsonProperty("total_number_of_hazards")
    public int hazards;

    @JsonProperty("total_number_of_hazard_datasets")
    public int hazardDatasets;

    @JsonProperty("total_number_of_dfr3")
    public int dfr3;

    public long datasetSize;

    @JsonProperty("total_file_size_of_hazard_datasets_byte")
    public long hazardDatasetSize;

    public List<Integer> service;
    public IncoreLabQuota incoreLab;

    // made up fields


    public UserUsages() { }

    public static UserUsages fromJson(String usageJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(usageJson, UserUsages.class);
        } catch (Exception e) {
            log.error("Could not parse usage JSON. Returning Usage with zero values", e);
            return new UserUsages();
        }
    }

    public int getDatasets() {
        return this.datasets;
    }

    public void setDatasets(int datasets) {
        this.datasets = datasets;
    }

    public int getHazards() {
        return this.hazards;
    }

    public void setHazards(int hazards) {
        this.hazards = hazards;
    }

    public int getHazardDatasets() {
        return this.hazardDatasets;
    }

    public void setHazardDatasets(int hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public int getDfr3() {
        return this.dfr3;
    }

    public void setDfr3(int dfr3) {
        this.dfr3 = dfr3;
    }

    public long getDatasetSize() {
        return this.datasetSize;
    }

    public void setDatasetSize(long datasetSize) {
        this.datasetSize = datasetSize;
    }

    public long getHazardDatasetSize() {
        return this.hazardDatasetSize;
    }

    public void setHazardDatasetSize(long hazardDatasetSize) {
        this.hazardDatasetSize = hazardDatasetSize;
    }

    public IncoreLabQuota getIncoreLab(){
        return this.incoreLab;
    }

    public void setIncoreLab(IncoreLabQuota incoreLab){
        this.incoreLab = incoreLab;
    }

    public List<Integer> getService() {
        return this.service;
    }

    public void setService(List<Integer> service){
        this.service = service;
    }


}
