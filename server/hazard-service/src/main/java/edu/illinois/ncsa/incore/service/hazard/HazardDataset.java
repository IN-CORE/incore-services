/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

public class HazardDataset {
    // Hazard Raster
    public static final String DETERMINISTIC_HAZARD_SCHEMA = "deterministicHazardRaster";
    public static final String DETERMINISTIC_HAZARD_TYPE = "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.deterministicHazardRaster.v1.0";
    public static final String RASTER_FORMAT = "raster";
    public static final String HAZARD_TIF = "hazard.tif";

    public static final String ERGO_SPACE = "ergo";

    // Dataset fields
    public static final String DATA_TYPE = "dataType";
    public static final String TITLE = "title";
    public static final String SOURCE_DATASET = "sourceDataset";
    public static final String FORMAT = "format";
    public static final String DESCRIPTION = "description";
    public static final String SPACES = "spaces";
    public static final String DATASET_PARAMETER = "dataset";
    public static final String FILE_PARAMETER_ = "file";

    // Kong
    public static final String X_CREDENTIAL_USERNAME = "X-Credential-Username";

    // Dataset API
    public static final String  DATASETS_ENDPOINT = "data/api/datasets";
    public static final String  DATASETS_FILES = "files";
}
