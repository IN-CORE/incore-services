/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

public interface TornadoHazard {
    // Defined Parameters
    public static double[] efWindSpeed = {65, 86, 111, 136, 166, 200}; // used operational EF scale (mph)
    public static double[] ef0WidthRate = {100.0};
    public static double[] ef0LenRate = {100.0};
    public static double[] ef1WidthRate = {62.5, 37.5};
    public static double[] ef1LenRate = {42.6, 57.4};
    public static double[] ef2WidthRate = {47.5, 31.4, 21.1};
    public static double[] ef2LenRate = {36.7, 35.2, 28.1};
    public static double[] ef3WidthRate = {33.8, 20.2, 26.2, 19.8};
    public static double[] ef3LenRate = {32.1, 31.8, 24.4, 11.7};
    public static double[] ef4WidthRate = {27.3, 18.7, 19.0, 17.5, 17.5};
    public static double[] ef4LenRate = {21.2, 21.0, 27.8, 15.8, 14.2};
    public static double[] ef5WidthRate = {27.3, 19.9, 13.6, 13.8, 12.7, 12.7};
    public static double[] ef5LenRate = {14.9, 18.5, 24.2, 18.9, 10.3, 13.2};

    // Conversions
    public static final double YARD_TO_METERS = 0.9144;
    public static final double MILES_TO_KILOMETERS = 1.60934;
    public static final double MILES_TO_METERS = 1609.34;
    public static final double METERS_TO_DEGREES = 0.0000089982311916;

    // Units
    // Miles per hour
    public static String WIND_MPH = "mph";
    // Meters per second
    public static String WIND_MPS = "mps";

    // Shapefile schema fields for tornado wind field ef boxes
    public static final String TORNADO_SCHEMA_NAME = "tornadowindfield"; //$NON-NLS-1$
    public static final String TORNADO_HAZARD_TYPE = "http://localhost:8080/semantics/edu.illinois.ncsa.incore.tornado.hazard.schemas.tornadohazard.v1.0";
    public static final String SHAPEFILE_FORMAT = "shapefile";
    public static final String SIMULATION = "simulation"; //$NON-NLS-1$
    public static final String EF_RATING_FIELD = "ef_rating"; //$NON-NLS-1$
}
