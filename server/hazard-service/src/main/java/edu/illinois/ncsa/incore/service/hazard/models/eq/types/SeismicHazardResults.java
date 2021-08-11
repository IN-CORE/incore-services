/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SeismicHazardResults {
    private final String period;
    private final String demandType;
    private final String units;
    private final List<HazardResult> hazardResults;

    public SeismicHazardResults(String hazardType, String demandType, List<HazardResult> hazardResults) {
        this.period = hazardType;
        this.units = BaseAttenuation.getUnits(demandType);
        this.demandType = demandType;
        this.hazardResults = hazardResults;
    }

    public List<HazardResult> getHazardResults() {
        return hazardResults;
    }

    public String getPeriod() {
        return period;
    }

    public String getUnits() {
        return units;
    }

    public String getDemandType() {
        return demandType;
    }

}
