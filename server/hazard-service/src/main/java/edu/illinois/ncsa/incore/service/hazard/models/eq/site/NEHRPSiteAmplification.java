package edu.illinois.ncsa.incore.service.hazard.models.eq.site;

import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;

public class NEHRPSiteAmplification extends SiteAmplification {

    private double[][] shortPeriodFactors;
    private double[] shortPeriodIntervals;
    private double[][] longPeriodFactors;
    private double[] longPeriodIntervals;

    public NEHRPSiteAmplification() {
        initializeNEHRPSiteFactors();
    }

    private void initializeNEHRPSiteFactors()
    {
        shortPeriodFactors = new double[][] { { 0.8, 1.0, 1.2, 1.6, 2.5 }, { 0.8, 1.0, 1.2, 1.4, 1.7 }, { 0.8, 1.0, 1.1, 1.2, 1.2 },
                { 0.8, 1.0, 1.0, 1.1, 0.9 }, { 0.8, 1.0, 1.0, 1.0, 0.9 } };
        shortPeriodIntervals = new double[] { 0.25, 0.5, 0.75, 1.0, 1.25 };

        longPeriodFactors = new double[][] { { 0.8, 1.0, 1.7, 2.4, 3.5 }, { 0.8, 1.0, 1.6, 2.0, 3.2 }, { 0.8, 1.0, 1.5, 1.8, 2.8 },
                { 0.8, 1.0, 1.4, 1.6, 2.4 }, { 0.8, 1.0, 1.3, 1.5, 2.4 } };
        longPeriodIntervals = new double[] { 0.1, 0.2, 0.3, 0.4, 0.5 };
    }

    @Override
    public double getSiteAmplification(Site site, double hazardValue, int siteClass, String period) {
        if (period.equalsIgnoreCase(HazardUtil.PGA)) {
            period = "0.0"; //$NON-NLS-1$
        }

        if (period.equalsIgnoreCase("0.0") || period.equalsIgnoreCase("0.2") || period.equals("0.3")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return getShortPeriodAmplificationFactor(siteClass, hazardValue);
        } else if (period.equalsIgnoreCase("1.0") || period.equalsIgnoreCase("1") || period.equalsIgnoreCase(HazardUtil.PGV)) { //$NON-NLS-1$ //$NON-NLS-2$
            return getLongPeriodAmplificationFactor(siteClass, hazardValue);
        }
        return 1.0;
    }

    private double getShortPeriodAmplificationFactor(int siteClass, double hazardValue)
    {
        if (siteClass == 5) {
            int[] rows = getShortPeriodRows(hazardValue);
            if (rows[0] == rows[1]) {
                return shortPeriodFactors[rows[0]][4];
            } else {
                return linearInterpolateAmplification(shortPeriodIntervals[rows[0]], shortPeriodFactors[rows[0]][4],
                        shortPeriodIntervals[rows[1]], shortPeriodFactors[rows[1]][4], hazardValue);
            }
        } else {
            int[] rows = getShortPeriodRows(hazardValue);
            if (rows[0] == rows[1]) {
                return shortPeriodFactors[rows[0]][siteClass];
            } else {
                return linearInterpolateAmplification(shortPeriodIntervals[rows[0]], shortPeriodFactors[rows[0]][siteClass],
                        shortPeriodIntervals[rows[1]], shortPeriodFactors[rows[1]][siteClass], hazardValue);
            }
        }
    }

    private int[] getShortPeriodRows(double hazard)
    {
        int[] rows = new int[2];
        for (int index = 0; index < shortPeriodIntervals.length; index++) {
            double lowerBound = shortPeriodIntervals[index];
            if (index == 0) {
                double upperBound = shortPeriodIntervals[index + 1];
                if (hazard <= lowerBound) {
                    rows[0] = index;
                    rows[1] = index;

                    return rows;
                } else if (hazard > lowerBound && hazard <= upperBound) {
                    rows[0] = index;
                    rows[1] = index + 1;
                    return rows;
                }
            } else if (index == shortPeriodIntervals.length - 1) {
                if (hazard > lowerBound) {
                    rows[0] = index;
                    rows[1] = index;
                    return rows;
                }
            } else {
                double upperBound = shortPeriodIntervals[index + 1];
                if (hazard > lowerBound && hazard <= upperBound) {
                    rows[0] = index;
                    rows[1] = index + 1;
                    return rows;
                }
            }
        }

        return rows;
    }

    private double getLongPeriodAmplificationFactor(int siteClass, double hazardValue)
    {
        if (siteClass == 5) {
            int[] rows = getLongPeriodRows(hazardValue);
            if (rows[0] == rows[1]) {
                return longPeriodFactors[rows[0]][4];
            } else {
                return linearInterpolateAmplification(longPeriodIntervals[rows[0]], longPeriodFactors[rows[0]][4],
                        longPeriodIntervals[rows[1]], longPeriodFactors[rows[1]][4], hazardValue);
            }
        } else {
            int[] rows = getLongPeriodRows(hazardValue);
            if (rows[0] == rows[1]) {
                return longPeriodFactors[rows[0]][siteClass];
            } else {
                return linearInterpolateAmplification(longPeriodIntervals[rows[0]], longPeriodFactors[rows[0]][siteClass],
                        longPeriodIntervals[rows[1]], longPeriodFactors[rows[1]][siteClass], hazardValue);
            }
        }
    }

    private int[] getLongPeriodRows(double hazard)
    {
        int[] rows = new int[2];

        for (int index = 0; index < longPeriodIntervals.length; index++) {
            double lowerBound = longPeriodIntervals[index];
            if (index == 0) {
                double upperBound = longPeriodIntervals[index + 1];
                if (hazard <= lowerBound) {
                    rows[0] = index;
                    rows[1] = index;

                    return rows;
                } else if (hazard > lowerBound && hazard <= upperBound) {
                    rows[0] = index;
                    rows[1] = index + 1;
                    return rows;
                }
            } else if (index == longPeriodIntervals.length - 1) {
                if (hazard > lowerBound) {
                    rows[0] = index;
                    rows[1] = index;
                    return rows;
                }
            } else {
                double upperBound = longPeriodIntervals[index + 1];
                if (hazard > lowerBound && hazard <= upperBound) {
                    rows[0] = index;
                    rows[1] = index + 1;
                    return rows;
                }
            }
        }

        return rows;
    }

    private double linearInterpolateAmplification(double x0, double y0, double x1, double y1, double x)
    {
        // alpha is the same for both magnitudes since they share the same
        // distances
        double alpha = (x - x0) / (x1 - x0);
        double y = (1 - alpha) * y0 + alpha * y1;

        return y;
    }

}
