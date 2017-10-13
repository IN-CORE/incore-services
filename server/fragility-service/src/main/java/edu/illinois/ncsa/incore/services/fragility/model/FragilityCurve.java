package edu.illinois.ncsa.incore.services.fragility.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
@XmlSeeAlso({CustomExpressionFragilityCurve.class, PeriodBuildingFragilityCurve.class,
             StandardFragilityCurve.class, PeriodStandardFragilityCurve.class})
public abstract class FragilityCurve {
    public String description;

    public FragilityCurve() {

    }

    public FragilityCurve(String label) {
        this.description = label;
    }
}
