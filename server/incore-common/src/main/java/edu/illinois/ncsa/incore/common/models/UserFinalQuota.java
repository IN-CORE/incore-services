/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.common.AllocationConstants;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity("UserFinalQuota")
public class UserFinalQuota {
    @Id
    @Property("_id")
    private ObjectId id;

    private String username;

    @JsonProperty("applicationLimits")
    private UserUsages applicationLimits;

    public UserFinalQuota() {
        this.applicationLimits = new UserUsages();
        this.username = null;
//        this.datasets = AllocationConstants.NUM_DATASETS;
//        this.hazards = AllocationConstants.NUM_HAZARDS;
//        this.hazardDatasets = AllocationConstants.NUM_HAZARD_DATASETS;
//        this.dfr3 = AllocationConstants.NUM_DFR3;
//        this.datasetSize = AllocationConstants.DATASET_SIZE;
//        this.hazardDatasetSize = AllocationConstants.HAZARD_DATASET_SIZE;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getUsername() { return this.username; }

    public UserUsages getApplicationLimits() { return this.applicationLimits; }

    public void setApplicationLimits(UserUsages applicationLimits) { this.applicationLimits = applicationLimits; }

//package edu.illinois.ncsa.incore.common.models;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.morphia.annotations.Embedded;
//import dev.morphia.annotations.Entity;
//import edu.illinois.ncsa.incore.common.AllocationConstants;
//import org.apache.log4j.Logger;
//
//import javax.xml.bind.annotation.XmlRootElement;
//
//@Embedded
//public class UserFinalQuota {
//    private static final Logger log = Logger.getLogger(UserFinalQuota.class);
//
//    public String userFinalQuotaId;
//    public int datasets;
//    public int hazards;
//    public int hazardDatasets;
//    public int dfr3;
//    public long datasetSize;
//    public long hazardDatasetSize;
//
//    public UserFinalQuota() {
//        this.userFinalQuotaId = null;
//        this.datasets = AllocationConstants.NUM_DATASETS;
//        this.hazards = AllocationConstants.NUM_HAZARDS;
//        this.hazardDatasets = AllocationConstants.NUM_HAZARD_DATASETS;
//        this.dfr3 = AllocationConstants.NUM_DFR3;
//        this.datasetSize = AllocationConstants.DATASET_SIZE;
//        this.hazardDatasetSize = AllocationConstants.HAZARD_DATASET_SIZE;
//    }
//
//    public static UserFinalQuota fromJson(String quotaJson) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.readValue(quotaJson, UserFinalQuota.class);
//        } catch (Exception e) {
//            log.error("Could not parse usage JSON. Returning Usage with zero values", e);
//            return new UserFinalQuota();
//        }
//    }
//
//    public String getUserFinalQuotaId() { return this.userFinalQuotaId; }
//
//    public void setUserFinalQuotaId(String userFinalQuotaId) { this.userFinalQuotaId = userFinalQuotaId; }
//
//    public int getDatasets() {
//        return this.datasets;
//    }
//
//    public void setDatasets(int datasets) {
//        this.datasets = datasets;
//    }
//
//    public int getHazards() {
//        return this.hazards;
//    }
//
//    public void setHazards(int hazards) {
//        this.hazards = hazards;
//    }
//
//    public int getHazardDatasets() {
//        return this.hazardDatasets;
//    }
//
//    public void setHazardDatasets(int hazardDatasets) {
//        this.hazardDatasets = hazardDatasets;
//    }
//
//    public int getDfr3() {
//        return this.dfr3;
//    }
//
//    public void setDfr3(int dfr3) {
//        this.dfr3 = dfr3;
//    }
//
//    public long getDatasetSize() {
//        return this.datasetSize;
//    }
//
//    public void setDatasetSize(int datasetSize) {
//        this.datasetSize = datasetSize;
//    }
//
//    public long getHazardDatasetSize() {
//        return this.hazardDatasetSize;
//    }
//
//    public void setHazardDatasetSize(int hazardDatasetSize) {
//        this.hazardDatasetSize = hazardDatasetSize;
//    }
}
