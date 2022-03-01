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
@Entity("UserAllocations")
public class UserAllocations {
    @Id
    @Property("_id")
    private ObjectId id;

    private String username;
//    private int datasets;
//    private int hazards;
//    private int hazardDatasets;
//    private int dfr3;
//    private long datasetSize;
//    private long hazardDatasetSize;

    @JsonProperty("usage")
    private UserUsages usage;

    public UserAllocations() {
        this.usage = new UserUsages();
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

    public UserUsages getUsage() { return this.usage; }

    public void setUsage(UserUsages usage) { this.usage = usage; }

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
