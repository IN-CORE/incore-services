/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tsunami.types;

public class TsunamiHazardResult {
    private String demand;
    private String units;
    private double hazardValue;
    private double latitude;
    private double longitude;

    public TsunamiHazardResult(double latitude, double longitude, double hazardValue, String demandType, String demandUnits) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hazardValue = hazardValue;
        this.demand = demandType;
        this.units = demandUnits;
    }

    public double getHazardValue() {
        return hazardValue;
    }

    public String getUnits() {
        return units;
    }

    public String getDemand() {
        return demand;
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
