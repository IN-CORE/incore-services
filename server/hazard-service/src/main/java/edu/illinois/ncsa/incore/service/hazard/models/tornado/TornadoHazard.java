/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;


public interface TornadoHazard {
    // Defined Parameters
    double[] efWindSpeed = {65, 86, 111, 136, 166, 200}; // used operational EF scale (mph)
    double[] ef0WidthRate = {100.0};
    double[] ef0LenRate = {100.0};
    double[] ef1WidthRate = {62.5, 37.5};
    double[] ef1LenRate = {42.6, 57.4};
    double[] ef2WidthRate = {47.5, 31.4, 21.1};
    double[] ef2LenRate = {36.7, 35.2, 28.1};
    double[] ef3WidthRate = {33.8, 20.2, 26.2, 19.8};
    double[] ef3LenRate = {32.1, 31.8, 24.4, 11.7};
    double[] ef4WidthRate = {27.3, 18.7, 19.0, 17.5, 17.5};
    double[] ef4LenRate = {21.2, 21.0, 27.8, 15.8, 14.2};
    double[] ef5WidthRate = {27.3, 19.9, 13.6, 13.8, 12.7, 12.7};
    double[] ef5LenRate = {14.9, 18.5, 24.2, 18.9, 10.3, 13.2};

    // Conversions
    double YARD_TO_METERS = 0.9144;
    double MILES_TO_KILOMETERS = 1.60934;
    double MILES_TO_METERS = 1609.34;
    double METERS_TO_DEGREES = 0.0000089982311916;

    // Units
    // Miles per hour
    String WIND_MPH = "mph";
    // Meters per second
    String WIND_MPS = "mps";

    String DEMAND_TYPE = "wind";

    // Shapefile schema fields for tornado wind field ef boxes
    String SHAPEFILE_FORMAT = "shapefile";
    String SIMULATION = "simulation"; //$NON-NLS-1$
    String EF_RATING_FIELD = "ef_rating"; //$NON-NLS-1$
}
