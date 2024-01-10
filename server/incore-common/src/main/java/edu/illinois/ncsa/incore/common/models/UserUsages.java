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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


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

    @JsonProperty("total_file_size_of_datasets_byte")
    public long datasetSize;

    @JsonProperty("total_file_size_of_hazard_datasets_byte")
    public long hazardDatasetSize;

    public List<Integer> service;
    public IncoreLabQuota incoreLab;

    // made up fields
    @JsonProperty("total_file_size_of_datasets")
    public String outDatasetSize;

    @JsonProperty("total_file_size_of_hazard_datasets")
    public String outHazardSize;

    public String group;
    public String user;

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

    public void setOutDatasetSize(){
        long datasetFileSize = this.getDatasetSize();
        double dataseSizeKb = datasetFileSize / 1024;
        double datasetSizeMb = dataseSizeKb / 1024;
        double datasetSizeGb = datasetSizeMb / 1024;
        dataseSizeKb = Math.round(dataseSizeKb * 100.0) / 100.0;
        datasetSizeMb = Math.round(datasetSizeMb * 100.0) / 100.0;
        datasetSizeGb = Math.round(datasetSizeGb * 100.0) / 100.0;

        String outDatasetSize;
        if (datasetSizeGb >= 1) {
            outDatasetSize = datasetSizeGb + " GB";
        } else if (datasetSizeMb >= 1) {
            outDatasetSize = datasetSizeMb + " MB";
        } else {
            outDatasetSize = dataseSizeKb + " KB";
        }
        this.outDatasetSize = outDatasetSize;
    }

    public String getOutHazardSize(){
        return this.outHazardSize;
    }

    public void setOutHazardSize(){

        long hazardFileSize = this.getHazardDatasetSize();

        double hazardSizeKb = hazardFileSize / 1024;
        double hazardSizeMb = hazardSizeKb / 1024;
        double hazardSizeGb = hazardSizeMb / 1024;

        // round values

        hazardSizeKb = Math.round(hazardSizeKb * 100.0) / 100.0;
        hazardSizeMb = Math.round(hazardSizeMb * 100.0) / 100.0;
        hazardSizeGb = Math.round(hazardSizeGb * 100.0) / 100.0;

        String outHazardSize;
        if (hazardSizeGb >= 1) {
            outHazardSize = hazardSizeGb + " GB";
        } else if (hazardSizeMb >= 1) {
            outHazardSize = hazardSizeMb + " MB";
        } else {
            outHazardSize = hazardSizeKb + " KB";
        }
        this.outHazardSize = outHazardSize;
    }

    public String getGroup(){
        return this.group;
    }

    public void setGroup(String group){
        this.group = group;
    }

    public String getUser(){
        return this.user;
    }

    public void setUser(String user){
        this.user = user;
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
