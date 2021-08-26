/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.types;

public class HurricaneHazardResult {
    private final String demand;
    private final String units;
    private final Double hazardValue;
    private double latitude;
    private double longitude;

    public HurricaneHazardResult(double latitude, double longitude, Double hazardValue, String demandType, String demandUnits) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hazardValue = hazardValue;
        this.demand = demandType;
        this.units = demandUnits;
    }

    public Double getHazardValue() {
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
