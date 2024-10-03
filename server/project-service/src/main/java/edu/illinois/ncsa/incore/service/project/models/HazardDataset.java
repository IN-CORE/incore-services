/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.project.models;
import dev.morphia.annotations.Embedded;

@Embedded
public abstract class HazardDataset {
    public String datasetId;
    public String demandType;
    public String demandUnits;
    public Double threshold = null;
}
