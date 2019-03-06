/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

public class HazardResult {

    private double latitude;
    private double longitude;
    private double hazardValue;

    public HazardResult(double latitude, double longitude, double hazardValue) {
       this.latitude = latitude;
       this.longitude = longitude;
       this.hazardValue = hazardValue;
    }
    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }
    public double getHazardValue() {
        return hazardValue;
    }
}
