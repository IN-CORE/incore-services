/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

import java.util.Arrays;
import java.util.List;

public class HazardConstants {
    // Hazard Raster
    public static final String PROBABILISTIC_HAZARD_SCHEMA = "probabilisticHazardRaster";
    public static final String DETERMINISTIC_HAZARD_SCHEMA = "deterministicHazardRaster";
    public static final String PROBABILISTIC_TSUNAMI_HAZARD_SCHEMA = "probabilisticTsunamiRaster";
    public static final String DETERMINISTIC_TSUNAMI_HAZARD_SCHEMA = "deterministicTsunamiRaster";
    public static final String PROBABILISTIC_HURRICANE_HAZARD_SCHEMA = "probabilisticHurricaneRaster";
    public static final String DETERMINISTIC_HURRICANE_HAZARD_SCHEMA = "deterministicHurricaneRaster";
    public static final String DETERMINISTIC_FLOOD_HAZARD_SCHEMA = "deterministicFloodRaster";
    public static final String PROBABILISTICs_FLOOD_HAZARD_SCHEMA = "deterministicFloodRaster";
    public static final String DETERMINISTIC_HAZARD_TYPE = "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.deterministicHazardRaster.v1.0";
    public static final String RASTER_FORMAT = "raster";
    public static final String HAZARD_TIF = "hazard.tif";

    // Dataset fields
    public static final String DATA_TYPE = "dataType";
    public static final String TITLE = "title";
    public static final String SOURCE_DATASET = "sourceDataset";
    public static final String FORMAT = "format";
    public static final String DESCRIPTION = "description";
    public static final String DATASET_PARAMETER = "dataset";
    public static final String FILE_PARAMETER_ = "file";

    // Ambassador
    public static final String X_AUTH_USERINFO = "x-auth-userinfo";

    // Dataset API
    public static final String DATASETS_ENDPOINT = "data/api/datasets";
    public static final String DATASETS_FILES = "files";

    public static List<String> EQ_DATASET_TYPES_ALLOWED = Arrays.asList("tif", "tiff");
    public static List<String> TORNADO_DATASET_TYPES_ALLOWED = Arrays.asList("shp");
}
