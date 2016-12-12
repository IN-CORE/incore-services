package edu.illinois.ncsa.Fragility.Model;

import edu.illinois.ncsa.Fragility.JaxPModel.FragilityDataset;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public static List<FragilityCurve> parseCurves(FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetLabels labels,
                                                   FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetFragilities curves) {
        List<FragilityCurve> createdCurves = new ArrayList<>();

        int i = 0;
        for (FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetFragilities.FragilityCurve curve : curves.getFragilityCurve()) {

            FragilityCurve createdCurve = null;

            String label = labels.getFragilitySetLabel().get(i);
            BigDecimal median = curve.getFragilityCurveMedian();
            BigDecimal beta = curve.getFragilityCurveBeta();

            // CustomExpressionFragility
            if (curve.getCurveType() != null && curve.getCurveType().contains("CustomExpressionFragility")) {
                String expression = curve.getExpression();
                createdCurve = new CustomExpressionFragilityCurve(expression, label);
            } else {
                if (curve.getCurveType() != null) {
                    int periodEqnType = curve.getPeriodEqnType();
                    double periodParam0 = curve.getPeriodParam0();
                    double periodParam1 = curve.getPeriodParam1();
                    double periodParam2 = curve.getPeriodParam2();

                    if (curve.getCurveType().contains("PeriodStandardFragilityCurve")) {
                        FragilityCurveType fragilityCurveType = FragilityCurveType.valueOf(curve.getFragilityCurveType());

                        createdCurve = new PeriodStandardFragilityCurve(median, beta, fragilityCurveType, label, periodEqnType,
                                                                        periodParam0, periodParam1, periodParam2);

                    } else if (curve.getCurveType().contains("PeriodBuildingFragilityCurve")) {
                        double fsParam0 = curve.getFsParam0();
                        double fsParam1 = curve.getFsParam1();
                        double fsParam2 = curve.getFsParam2();
                        double fsParam3 = curve.getFsParam3();
                        double fsParam4 = curve.getFsParam4();
                        double fsParam5 = curve.getFsParam5();

                        createdCurve = new PeriodBuildingFragilityCurve(label, periodEqnType, periodParam0, periodParam1, periodParam2,
                                                                        fsParam0, fsParam1, fsParam2, fsParam3, fsParam4, fsParam5);
                    } else {
                        System.out.print("Unknown type");
                    }
                } else {
                    FragilityCurveType fragilityCurveType = FragilityCurveType.valueOf(curve.getFragilityCurveType());

                    createdCurve = new StandardFragilityCurve(median, beta, fragilityCurveType, label);
                }
            }

            i++;
            createdCurves.add(createdCurve);
        }

        return createdCurves;
    }
}
