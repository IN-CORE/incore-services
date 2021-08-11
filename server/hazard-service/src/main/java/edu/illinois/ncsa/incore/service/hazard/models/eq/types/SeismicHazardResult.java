/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SeismicHazardResult {
    private final String period;
    private String demand;
    private final String units;
    private final double hazardValue;

    public SeismicHazardResult(double hazardValue, String hazardType, String demand, String demandUnits) {
        this.hazardValue = hazardValue;
        this.period = hazardType;
        this.units = demandUnits;
        this.demand = demand;
    }

    public SeismicHazardResult(double hazardValue, String hazardType, String demand) {
        this.hazardValue = hazardValue;
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(demand);
        this.demand = demand;
    }

    public SeismicHazardResult(double hazardValue, String hazardType) {
        this.hazardValue = hazardValue;
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(hazardType);
    }

    public double getHazardValue() {
        return hazardValue;
    }

    public String getPeriod() {
        return period;
    }

    public String getUnits() {
        return units;
    }

    public String getDemand() {
        return demand;
    }
}
