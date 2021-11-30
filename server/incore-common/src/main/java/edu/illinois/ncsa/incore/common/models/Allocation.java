/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import edu.illinois.ncsa.incore.common.AllocationConstants;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Created by ywkim on 10/2/2017.
 */

@XmlRootElement
@Entity("Allocation")
public class Allocation {
    @Id
    @Property("_id")
    private ObjectId id;

    private String space_id;
    private int datasets;
    private int hazards;
    private int hazardDatasets;
    private int dfr3;
    private long datasetSize;
    private long hazardDatasetSize;

    public Allocation() {
        this.space_id = null;
        this.datasets = AllocationConstants.NUM_DATASETS;
        this.hazards = AllocationConstants.NUM_HAZARDS;
        this.hazardDatasets = AllocationConstants.NUM_HAZARD_DATASETS;
        this.dfr3 = AllocationConstants.NUM_DFR3;
        this.datasetSize = AllocationConstants.DATASET_SIZE;
        this.hazardDatasetSize = AllocationConstants.HAZARD_DATASET_SIZE;
    }

}
