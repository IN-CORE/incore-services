/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.liquefaction;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

public class HazusLiquefaction implements Liquefaction {
    private static final Logger logger = Logger.getLogger(HazusLiquefaction.class);
    public final static String ID = "hazus-liquefaction";

    private static final String LIQ_SUSCEPTIBILITY_NONE = "None";
    private static final String LIQ_SUSCEPTIBILITY_VERYLOW = "Very Low";
    private static final String LIQ_SUSCEPTIBILITY_LOW = "Low";
    private static final String LIQ_SUSCEPTIBILITY_MODERATE = "Moderate";
    private static final String LIQ_SUSCEPTIBILITY_HIGH = "High";
    private static final String LIQ_SUSCEPTIBILITY_VERYHIGH = "Very High";

    private final double defaultGroundWaterDepth = 5.0; // HAZUS Assumption

    public HazusLiquefaction() {
    }

    /**
     * @param susceptibility_level
     * @param pga_value
     * @param ground_water_depth
     * @param magnitude
     * @return
     */
    private double[] computeGroundFailureProbability(String susceptibility_level, double pga_value, double ground_water_depth,
                                                     double magnitude) {
        double[] p_gf = new double[3];
        if (susceptibility_level != null && pga_value > 0.0) {
            // double magnitude = Double.parseDouble( pgaHazard.getAttrib(
            // ATT_MAGNITUDE ) );
            // ground_water_depth = getGroundWaterDepth( location );
            double p_ml = getProportionOfMapUnit(susceptibility_level);
            double k_m = getMomentMagnitudeCorrectionFactor(magnitude);
            double k_w = getGroundWaterCorrectionFactor(ground_water_depth);
            double uncorrected_liq_prob = getUncorrectedLiquefactionProbability(pga_value, susceptibility_level);
            double corrected_liq_prob = getCorrectedLiquefactionProbability(uncorrected_liq_prob, p_ml, k_m, k_w);

            double lateral_spreading = getLateralSpreading(susceptibility_level, pga_value, magnitude);
            double settlement = getGroundSettlement(susceptibility_level);

            double[] p_gf_lateralspreading = computeLateralSpreadingGroundfailureProbability(lateral_spreading, corrected_liq_prob, 0);
            double[] p_gf_settlement = computeGroundSettlementGroundFailureProbability(settlement, corrected_liq_prob, 0);
            p_gf = getGroundFailureProbability(p_gf_lateralspreading, p_gf_settlement);
        }

        return p_gf;
    }

    /**
     * @param magnitude
     * @param pgaValue
     * @param susceptibility
     * @param groundWaterDepth - specify -1 to use default ground water depth
     * @return
     */
    public double getProbabilityOfLiquefaction(double magnitude, double pgaValue, String susceptibility, double groundWaterDepth) {
        double siteGroundWaterDepth = groundWaterDepth;
        if (susceptibility != null && pgaValue > 0.0) {
            if (siteGroundWaterDepth == -1) {
                siteGroundWaterDepth = defaultGroundWaterDepth;
            }
            double p_ml = getProportionOfMapUnit(susceptibility);
            double k_m = getMomentMagnitudeCorrectionFactor(magnitude);
            double k_w = getGroundWaterCorrectionFactor(siteGroundWaterDepth);
            double uncorrected_liq_prob = getUncorrectedLiquefactionProbability(pgaValue, susceptibility);
            double corrected_liq_prob = getCorrectedLiquefactionProbability(uncorrected_liq_prob, p_ml, k_m, k_w);

            return corrected_liq_prob;
        }
        return 0;
    }

    /**
     * @param susceptibility_level
     * @param pga_value
     * @param magnitude
     * @return
     */
    private double computeLateralSpreading(String susceptibility_level, double pga_value, double magnitude) {
        // double magnitude = Double.parseDouble( pgaHazard.getAttrib(
        // ATT_MAGNITUDE ) );
        // Lateral Spreading
        double k_delta = getKDeltaTerm(magnitude);
        double pga_t = getThresholdPGA(susceptibility_level);

        double lateral_spreading = 0.0;
        if (pga_t != -1) {
            double pga_pgat_ratio = pga_value / pga_t;
            // lateral_spreading = computeLateralSpreading( pga_pgat_ratio,
            // k_delta );
            lateral_spreading = getDisplacement(pga_pgat_ratio) * k_delta;
        }
        return lateral_spreading;
    }

    /**
     * @param susceptibility_level
     * @return
     */
    private double computeExpectedGroundSettlement(String susceptibility_level) {

        double expected_settlement = 0.0;
        if (susceptibility_level != null) { // && pga_value > 0.0 ) {
            expected_settlement = this.getExpectedGroundSettlement(susceptibility_level);
            // settlement = corrected_liq_prob * expected_settlement;
        }

        return expected_settlement;
    }

    /**
     * @param pga_pgat_ratio
     * @return
     */
    private double getDisplacement(double pga_pgat_ratio) {
        double displacement = 0.0;

        if (pga_pgat_ratio != -1) {
            if (pga_pgat_ratio < 1.0) {
                displacement = 0.0;
            } else if (pga_pgat_ratio >= 1.0 && pga_pgat_ratio < 2.0) {
                displacement = 12.0 * pga_pgat_ratio - 12.0;
            } else if (pga_pgat_ratio >= 2.0 && pga_pgat_ratio < 3.0) {
                displacement = 18 * pga_pgat_ratio - 24.0;
            } else if (pga_pgat_ratio >= 3.0 && pga_pgat_ratio < 4.0) {
                displacement = 70.0 * pga_pgat_ratio - 180.0;
            } else {
                displacement = 100.0;
            }
        }

        // logger.debug( "uncorrected displacement = " + displacement );
        return displacement;
    }

    /**
     * @param susceptibility_level
     * @return
     */
    private double getProportionOfMapUnit(String susceptibility_level) {
        double p_ml = 0.0;
        if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYHIGH)) {
            p_ml = 0.25;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_HIGH)) {
            p_ml = 0.20;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_MODERATE)) {
            p_ml = 0.10;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_LOW)) {
            p_ml = 0.05;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYLOW)) {
            p_ml = 0.02;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_NONE)) {
            // base case handled at initialization of p_ml
        }

        return p_ml;
    }

    /**
     * @param magnitude
     * @return
     */
    private double getMomentMagnitudeCorrectionFactor(double magnitude) {
        double k_m = 0.0027 * Math.pow(magnitude, 3) - 0.0267 * Math.pow(magnitude, 2) - 0.2055 * magnitude + 2.9188;
        return k_m;
    }

    /**
     * @param groundwaterDepth
     * @return
     */
    private double getGroundWaterCorrectionFactor(double groundwaterDepth) {
        double k_w = 0.022 * groundwaterDepth + 0.93; // ground water depth
        // must be in units of
        // feet
        return k_w;
    }

    /**
     * @param pga
     * @param susceptibility_level
     * @return
     */
    private double getUncorrectedLiquefactionProbability(double pga, String susceptibility_level) {
        double liq_prob_unc = 0.0;

        if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYHIGH)) {
            liq_prob_unc = 9.09 * pga - 0.82;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_HIGH)) {
            liq_prob_unc = 7.67 * pga - 0.82;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_MODERATE)) {
            liq_prob_unc = 6.67 * pga - 0.82;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_LOW)) {
            liq_prob_unc = 5.57 * pga - 0.82;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYLOW)) {
            liq_prob_unc = 4.16 * pga - 0.82;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_NONE)) {
            // base case handled at initialization
        }

        return liq_prob_unc;
    }

    /**
     * @param liq_prob_unc
     * @param p_ml
     * @param k_m
     * @param k_w
     * @return
     */
    private double getCorrectedLiquefactionProbability(double liq_prob_unc, double p_ml, double k_m, double k_w) {
        double liq_prob_adj = liq_prob_unc / (k_m * k_w);
        if (liq_prob_adj > 1.0) {
            liq_prob_adj = 1.0;
        } else if (liq_prob_adj < 0.0) {
            liq_prob_adj = 0.0;
        }

        double liq_prob = liq_prob_adj * p_ml;
        return liq_prob;
    }

    /**
     * @param magnitude
     * @return
     */
    private double getKDeltaTerm(double magnitude) {
        double k_delta = 0.0086 * Math.pow(magnitude, 3) - 0.0914 * Math.pow(magnitude, 2) + 0.4698 * magnitude - 0.9835;
        return k_delta;
    }

    /**
     * @param susceptibility
     * @return
     */
    private double getThresholdPGA(String susceptibility) {
        double pga_t = -1; // if negative, means no liquefaction susceptibility
        if (susceptibility == null) {
            return pga_t;
        }
        if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYHIGH)) {
            pga_t = 0.09;
        } else if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_HIGH)) {
            pga_t = 0.12;
        } else if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_MODERATE)) {
            pga_t = 0.15;
        } else if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_LOW)) {
            pga_t = 0.21;
        } else if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYLOW)) {
            pga_t = 0.26;
        } else if (susceptibility.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_NONE)) {
            // base case handled at initialization
        }

        return pga_t;
    }

    /**
     * @param susceptibility_level
     * @return
     */
    private double getExpectedGroundSettlement(String susceptibility_level) {
        double settlement = 0.0;
        if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYHIGH)) {
            settlement = 12.0; // unit is inches
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_HIGH)) {
            settlement = 6.0;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_MODERATE)) {
            settlement = 2.0;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_LOW)) {
            settlement = 1.0;
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_VERYLOW)) {
            // base case handled at initialization
        } else if (susceptibility_level.equalsIgnoreCase(LIQ_SUSCEPTIBILITY_NONE)) {
            // base case handled at initialization
        }

        return settlement;
    }

    /**
     * @param lateral_spreading
     * @param liq_prob
     * @param foundation        0 - Shallow, 1 - Deep
     * @return
     */
    private double[] computeLateralSpreadingGroundfailureProbability(double lateral_spreading, double liq_prob, int foundation) {
        double[] p_gf = new double[3];
        double mean = 0.0;
        double standard_deviation = 1.0;
        NormalDistribution solver = new NormalDistribution(mean, standard_deviation);

        double foundation_mult = 1.0;

        if (foundation == 1)
            foundation_mult = 0.5;

        if (lateral_spreading == 0) {
            return p_gf;
        }
        double x = (Math.log(lateral_spreading) - Math.log(60)) / 1.2;

        p_gf[0] = solver.cumulativeProbability(x) * liq_prob * foundation_mult;
        p_gf[1] = p_gf[0];
        p_gf[2] = p_gf[0] * 0.2;

        return p_gf;
    }

    /**
     * @param settlement
     * @param liq_prob
     * @param foundation 0 - Shallow, 1 - Deep
     * @return
     */
    private double[] computeGroundSettlementGroundFailureProbability(double settlement, double liq_prob, double foundation) {
        double[] p_gf = new double[3];
        double mean = 0.0;
        double standard_deviation = 1.0;
        NormalDistribution solver = new NormalDistribution(mean, standard_deviation);

        double foundation_mult = 1.0;

        if (foundation == 1)
            foundation_mult = 0.1;

        if (settlement == 0) {
            return p_gf;
        }
        double x = (Math.log(settlement) - Math.log(10)) / 1.2;

        p_gf[0] = solver.cumulativeProbability(x) * liq_prob * foundation_mult;
        p_gf[1] = p_gf[0];
        p_gf[2] = p_gf[0] * 0.2;

        return p_gf;
    }

    /**
     * @param p_gf_lateralspreading
     * @param p_gf_settlement
     * @return
     */
    private double[] getGroundFailureProbability(double[] p_gf_lateralspreading, double[] p_gf_settlement) {
        double[] p_gf = new double[3];

        for (int i = 0; i < 3; i++) {
            p_gf[i] = Math.max(p_gf_lateralspreading[i], p_gf_settlement[i]);
        }

        return p_gf;
    }

    /**
     * @param susceptibility_level
     * @param pga_value
     * @param ground_water_depth
     * @param magnitude
     * @return
     */
    public double[] getProbabilityOfGroundFailure(String susceptibility_level, double pga_value, double ground_water_depth,
                                                  double magnitude) {
        return computeGroundFailureProbability(susceptibility_level, pga_value, ground_water_depth, magnitude);
    }

    /**
     * @param susceptibility_level
     * @param pga_value
     * @param magnitude
     * @return
     */
    public double getLateralSpreading(String susceptibility_level, double pga_value, double magnitude) {
        return computeLateralSpreading(susceptibility_level, pga_value, magnitude);
    }

    /**
     * @return
     */
    public double getPermanentGroundDeformation(String susceptibility, double pgaValue, double magnitude) {
        return Math.max(getLateralSpreading(susceptibility, pgaValue, magnitude), getGroundSettlement(susceptibility));
    }

    /**
     * @param susceptibility_level
     * @return
     */
    public double getGroundSettlement(String susceptibility_level) {
        return computeExpectedGroundSettlement(susceptibility_level);
    }

    /**
     * @return
     */
    public String getLiquefactionTypeId() {
        return ID;
    }

}
