package edu.illinois.ncsa.incore.common.utils;

public class GeoUtils {
    public static enum gpkgValidationResult {
        VALID,
        RASTER_OR_NO_VECTOR_LAYER,
        MULTIPLE_VECTOR_LAYERS,
        NAME_MISMATCH
    }
}
