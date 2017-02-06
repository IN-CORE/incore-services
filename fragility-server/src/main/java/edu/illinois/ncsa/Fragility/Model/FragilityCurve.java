package edu.illinois.ncsa.Fragility.Model;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
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
