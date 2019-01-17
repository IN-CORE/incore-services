package edu.illinois.ncsa.incore.service.hazard.models.hurricane.types;

public enum WindfieldDemandUnits {
    kt,
    mps,
    kmph;

    public static WindfieldDemandUnits fromString(String unit) {
        String lowerUnit = unit.toLowerCase();
        return valueOf(lowerUnit);
    }

}
