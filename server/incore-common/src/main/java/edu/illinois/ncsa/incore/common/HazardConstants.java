/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HazardConstants {
    // Hazard Raster
    public static final String PROBABILISTIC_EARTHQUAKE_HAZARD_SCHEMA = "ergo:probabilisticEarthquakeRaster";
    public static final String DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA = "ergo:deterministicEarthquakeRaster";
    public static final String PROBABILISTIC_TSUNAMI_HAZARD_SCHEMA = "incore:probabilisticTsunamiRaster";
    public static final String DETERMINISTIC_TSUNAMI_HAZARD_SCHEMA = "incore:deterministicTsunamiRaster";
    public static final String PROBABILISTIC_HURRICANE_HAZARD_SCHEMA = "incore:probabilisticHurricaneRaster";
    public static final String DETERMINISTIC_HURRICANE_HAZARD_SCHEMA = "incore:deterministicHurricaneRaster";
    public static final String HURRICANE_GRID_SNAPSHOT_HAZARD_SCHEMA = "incore:hurricaneGridSnapshot";
    public static final String TORNADO_WINDFIELD_SCHEMA = "incore:tornadoWindfield";
    public static final String DETERMINISTIC_FLOOD_HAZARD_SCHEMA = "incore:deterministicFloodRaster";
    public static final String PROBABILISTIC_FLOOD_HAZARD_SCHEMA = "incore:probabilisticFloodRaster";
    public static final String RASTER_FORMAT = "raster";
    public static final String SHAPEFILE_FORMAT = "shapefile";
    public static final String HAZARD_TIF = "hazard.tif";

    public static final List<String> DATA_TYPE_HAZARD = new ArrayList<>(Arrays.asList(
        PROBABILISTIC_EARTHQUAKE_HAZARD_SCHEMA,
        DETERMINISTIC_EARTHQUAKE_HAZARD_SCHEMA,
        PROBABILISTIC_TSUNAMI_HAZARD_SCHEMA,
        DETERMINISTIC_TSUNAMI_HAZARD_SCHEMA,
        PROBABILISTIC_HURRICANE_HAZARD_SCHEMA,
        DETERMINISTIC_HURRICANE_HAZARD_SCHEMA,
        HURRICANE_GRID_SNAPSHOT_HAZARD_SCHEMA,
        TORNADO_WINDFIELD_SCHEMA,
        DETERMINISTIC_FLOOD_HAZARD_SCHEMA,
        PROBABILISTIC_FLOOD_HAZARD_SCHEMA,
        "ergo:hazardRaster"));

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
    public static List<String> TORNADO_DATASET_TYPES_ALLOWED = Arrays.asList("shp", "zip");

}
