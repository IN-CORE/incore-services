/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.flood.utils;

import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodConstants;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FloodUtil {
    // Metric
    private static final String units_m = "m";
    private static final String units_cm = "cm";

    // Imperial
    private static final String units_in = "in";
    private static final String units_ft = "ft";

    // Time
    private static final String units_hr = "hr";
    private static final String units_min = "min";

    public static final List<String> distanceUnits = Arrays.asList(units_cm, units_m, units_in, units_ft);

    private static final double m_to_cm = 100.0;
    private static final double m_to_in = 39.3701;
    private static final double m_to_ft = 3.28084;

    private static final double in_to_cm = 2.54;
    private static final double in_to_m = 0.0254;
    private static final double in_to_ft = 0.0833333;

    private static final double cm_to_m = 0.01;
    private static final double cm_to_in = 0.3937;
    private static final double cm_to_ft = 0.0328;

    private static final double ft_to_cm = 30.48;
    private static final double ft_to_in = 12;
    private static final double ft_to_m = 0.3048;

    public static double convertHazard(double hazardValue, String demandType, String originalDemandUnits,
                                       String requestedDemandUnits) throws UnsupportedOperationException, IllegalAccessException,
        NoSuchFieldException {
        double convertedHazardValue = hazardValue;
        if (demandType.equalsIgnoreCase(FloodConstants.DEPTH) || demandType.equalsIgnoreCase(FloodConstants.WSE)) {
            if (distanceUnits.contains(requestedDemandUnits.toLowerCase())) {
                if (!originalDemandUnits.equalsIgnoreCase(requestedDemandUnits)) {
                    String conversion = originalDemandUnits + "_to_" + requestedDemandUnits;
                    double conversionValue = FloodUtil.getVariableValue(conversion);
                    convertedHazardValue = conversionValue * hazardValue;
                }
            } else {
                throw new UnsupportedOperationException("Invalid demandUnits provided" + requestedDemandUnits);
            }
        } else {
            throw new UnsupportedOperationException("Invalid demandType provided" + demandType);
        }
        return convertedHazardValue;
    }

    /*
    Get values dynamically from variable name. ex: "cm_to_in"
     */
    public static double getVariableValue(String variableName) throws IllegalAccessException, NoSuchFieldException {
        Field field = FloodUtil.class.getDeclaredField(variableName);
        return field.getDouble(null);
    }


}
