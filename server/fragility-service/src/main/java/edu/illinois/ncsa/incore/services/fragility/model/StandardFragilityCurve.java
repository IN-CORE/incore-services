package edu.illinois.ncsa.incore.services.fragility.model;

import java.math.BigDecimal;

public class StandardFragilityCurve extends FragilityCurve {
    public BigDecimal median;
    public BigDecimal beta;
    public FragilityCurveType curveType;

    public StandardFragilityCurve() {
        super();
    }

    public StandardFragilityCurve(BigDecimal median, BigDecimal beta, FragilityCurveType curveType, String label) {
        super(label);

        this.median = median;
        this.beta = beta;
        this.curveType = curveType;
    }
}
