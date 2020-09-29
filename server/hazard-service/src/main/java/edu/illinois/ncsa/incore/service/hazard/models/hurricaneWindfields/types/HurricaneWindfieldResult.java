/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.types;

public class HurricaneWindfieldResult {
    private String demandType;
    private String demandUnits;
    private double hazardValue;
    private double latitude;
    private double longitude;

    public HurricaneWindfieldResult(double latitude, double longitude, double hazardValue, String demandType, String demandUnits) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hazardValue = hazardValue;
        this.demandType = demandType;
        this.demandUnits = demandUnits;
    }

    public double getHazardValue() {
        return hazardValue;
    }

    public String getDemandUnits() {
        return demandUnits;
    }

    public String getDemandType() {
        return demandType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
