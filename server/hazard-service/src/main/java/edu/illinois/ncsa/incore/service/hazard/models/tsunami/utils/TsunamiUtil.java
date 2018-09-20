/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tsunami.utils;

import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiConstants;

public class TsunamiUtil {
    // Metric
    private static final String units_m = "m";
    private static final String units_cm = "cm";
    private static final String units_cm_per_s = "cm/s";
    private static final String units_m_per_s = "m/s";
    private static final String units_m3_per_s2 = "m^3/s^2";

    // Imperial
    private static final String units_in = "in";
    private static final String units_ft = "ft";
    private static final String units_in_per_s = "in/s";
    private static final String units_ft3_per_s2 = "ft^3/s^2";

    private static final double meters_to_cm = 100.0;
    private static final double meters_to_inches = 39.3701;
    private static final double meters_to_feet = 3.28084;

    private static final double inches_to_cm = 2.54;
    private static final double inches_to_meter = 0.0254;
    private static final double inches_to_feet = 0.0833333;

    public static double convertHazard(double hazardValue, String demandType, String originalDemandUnits, String requestedDemandUnits) throws UnsupportedOperationException {
        // We'll need to update this method over time if new units become common for a demand type
        double convertedHazardValue = hazardValue;
        if (!originalDemandUnits.equalsIgnoreCase(requestedDemandUnits)) {
            if (demandType.equalsIgnoreCase(TsunamiConstants.HMAX) || demandType.equalsIgnoreCase(TsunamiConstants.LEGACY_HMAX)) {
                hazardValue = getCorrectUnitsOfHMAX(hazardValue, originalDemandUnits, requestedDemandUnits);
            } else if (demandType.equalsIgnoreCase(TsunamiConstants.VMAX)) {
                hazardValue = getCorrectUnitsOfVMAX(hazardValue, originalDemandUnits, requestedDemandUnits);
            } else if (demandType.equalsIgnoreCase(TsunamiConstants.MMAX) || demandType.equalsIgnoreCase(TsunamiConstants.LEGACY_MMAX)) {
                hazardValue = getCorrectUnitsOfMMAX(hazardValue, originalDemandUnits, requestedDemandUnits);
            } else {
                throw new UnsupportedOperationException("Cannot convert from " + originalDemandUnits + " to " + requestedDemandUnits);
            }
        }

        return convertedHazardValue;
    }

    public static double getCorrectUnitsOfHMAX(double hazardValue, String originalDemandUnits, String requestedDemandUnits) {
        // There may be missing conversion we'll eventually need to support, but added a bunch of common ones for now
        if (originalDemandUnits.equalsIgnoreCase(units_m) && requestedDemandUnits.equalsIgnoreCase(units_ft)) {
            return hazardValue * meters_to_feet;
        } else if (originalDemandUnits.equalsIgnoreCase(units_m) && requestedDemandUnits.equalsIgnoreCase(units_in)) {
            return hazardValue * meters_to_inches;
        } else if (originalDemandUnits.equalsIgnoreCase(units_m) && requestedDemandUnits.equalsIgnoreCase(units_cm)) {
            return hazardValue * meters_to_cm;
        } else if (originalDemandUnits.equalsIgnoreCase(units_in) && requestedDemandUnits.equalsIgnoreCase(units_m)) {
            return hazardValue * inches_to_meter;
        } else if (originalDemandUnits.equalsIgnoreCase(units_in) && requestedDemandUnits.equalsIgnoreCase(units_cm)) {
            return hazardValue * inches_to_cm;
        } else if (originalDemandUnits.equalsIgnoreCase(units_in) && requestedDemandUnits.equalsIgnoreCase(units_ft)) {
            return hazardValue * inches_to_feet;
        } else {
            throw new UnsupportedOperationException("Cannot convert Hmax from " + originalDemandUnits + " to " + requestedDemandUnits);
        }
    }

    public static double getCorrectUnitsOfVMAX(double hazardValue, String originalDemandUnits, String requestedDemandUnits) {
        if (originalDemandUnits.equalsIgnoreCase(units_m_per_s) && requestedDemandUnits.equalsIgnoreCase(units_cm_per_s)) {
            return hazardValue * meters_to_cm;
        } else if (originalDemandUnits.equalsIgnoreCase(units_m_per_s) && requestedDemandUnits.equalsIgnoreCase(units_in_per_s)) {
            return hazardValue * meters_to_inches;
        } else if (originalDemandUnits.equalsIgnoreCase(units_in_per_s) && requestedDemandUnits.equalsIgnoreCase(units_m_per_s)) {
            return hazardValue * inches_to_meter;
        } else if (originalDemandUnits.equalsIgnoreCase(units_in_per_s) && requestedDemandUnits.equalsIgnoreCase(units_cm_per_s)) {
            return hazardValue * inches_to_cm;
        } else {
            throw new UnsupportedOperationException("Cannot convert Vmax from " + originalDemandUnits + " to " + requestedDemandUnits);
        }
    }

    public static double getCorrectUnitsOfMMAX(double hazardValue, String originalDemandUnits, String requestedDemandUnits) {
        if (originalDemandUnits.equalsIgnoreCase(units_m3_per_s2) && requestedDemandUnits.equalsIgnoreCase(units_ft3_per_s2)) {
            return hazardValue * Math.pow(meters_to_feet, 3);
        } else if (originalDemandUnits.equalsIgnoreCase(units_ft3_per_s2) && requestedDemandUnits.equalsIgnoreCase(units_m3_per_s2)) {
            return hazardValue / Math.pow(meters_to_feet, 3);
        } else {
            throw new UnsupportedOperationException("Cannot convert Mmax from " + originalDemandUnits + " to " + requestedDemandUnits);
        }
    }

}
