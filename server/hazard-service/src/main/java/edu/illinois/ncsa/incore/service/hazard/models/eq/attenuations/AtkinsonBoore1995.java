/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtkinsonBoore1995 extends BaseAttenuation {

    public AtkinsonBoore1995() {
        InputStream is = AtkinsonBoore1995.class.getResourceAsStream("/hazard/earthquake/coefficients/AtkinsonBoore1995.csv");
        readCoefficients(is);
    }

    @Override
    public double getValue(String period, Site site) throws Exception {

        double mag = ruptureParameters.getMagnitude();
        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        double distance = HazardUtil.findDistance(site.getLocation(), sourceSite.getLocation());
        return getValue(period, mag, distance, sourceSite.getDepth());
    }

    /**
     * @param medianHazard
     * @param period
     * @param site
     * @return Lognormal standard deviation
     * @throws Exception
     */
    public double getStandardDeviation(double medianHazard, String period, Site site) throws Exception {
        double std_deviation = 0.0;
        std_deviation = Math.sqrt(Math.pow(getAleatoricUncertainties(period), 2) +
            getEpistemicVariance(medianHazard, period, site));
        return std_deviation;
    }


    public Map<String, Double> getAleatoricUncertainties() {
        return new HashMap<String, Double>() {
            {
                put("PGA", 0.620);
                put("0.2 SA", 0.581);
                put("0.3 SA", 0.5730878);
                put("1.0 SA", 0.550);
            }
        };
    }

    @Override
    public boolean canProduceStandardDeviation() {
        return true;
    }

    /**
     * @param period     Period / Hazard to compute
     * @param magnitude  Moment Magnitude of the event
     * @param distance   Distance from the epicenter to the point of interest
     * @param focalDepth Depth of the event
     * @return Hazard Value
     * @todo We assume site class D, we must get this info from the user if
     * available
     */
    private double getValue(String period, double magnitude, double distance, double focalDepth) {
        List<Double> coeff = getCoefficients(period);
        double r = Math.sqrt(Math.pow(distance, 2) + Math.pow(focalDepth, 2));
        // Constraint provided by Glenn Rix
        if (r < 10.0)
            r = 10.0;

        // System.out.println("R_hypo = "+r);
        double val = coeff.get(0) + coeff.get(1) * (magnitude - 6) + coeff.get(2) * Math.pow((magnitude - 6), 2) - Math.log10(r) - coeff.get(3) * r;

        // Site A motions with units of g
        val = Math.pow(10, val) * 0.001019716;

        // Convert to B/C
        val = HazardUtil.convertMotionsToBC(period, val);

        return val;

    }

    /**
     * @return
     */
    @Override
    public boolean isFaultTypeRequired() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isGeologyRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isDipAngleRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isAzimuthAngleRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isCoseismicRuptureDepthRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isShearWaveDepthRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isShearWaveDepth10Required() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isRakeAngleRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isSeismogenicDepthRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    public boolean isSoilMapRequired() {
        return true;
    }

    /**
     * @return
     */
    public boolean isSoilTypeRequired() {
        return true;
    }

}
