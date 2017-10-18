/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;

import java.util.List;

public class AtkinsonBoore1995 extends BaseAttenuation {

    private static double[] aleatoricUncertainties = {0.620, 0.581, 0.5730878, 0.550};

    @Override
    public double getValue(String period, Site site) throws Exception {

        double mag = ruptureParameters.getMagnitude();
        double srcLatitude = ruptureParameters.getSrcLatitude();
        double srcLongitude = ruptureParameters.getSrcLongitude();
        double depth = ruptureParameters.getDepth();
        Site sourceSite = new Site(new GeometryFactory().createPoint(new Coordinate(srcLongitude, srcLatitude)), depth);

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
        std_deviation = Math.sqrt(Math.pow(getAleatoricStdDev(period, site), 2) + getEpistemicVariance(medianHazard, period, site));
        return std_deviation;
    }

    /**
     * @param period
     * @return Lognormal aleatoric uncertainty
     */
    public double getAleatoricStdDev(String period, Site site) throws Exception {
        if (period.equalsIgnoreCase("PGA")) {
            return aleatoricUncertainties[0];
        } else if (period.equalsIgnoreCase("0.2")) {
            return aleatoricUncertainties[1];
        } else if (period.equalsIgnoreCase("0.3")) {
            return aleatoricUncertainties[2];
        } else if (period.equalsIgnoreCase("1.0")) {
            return aleatoricUncertainties[3];
        } else {
            return 0.0;
        }
    }

    @Override
    public boolean canProduceStandardDeviation() {
        return true;
    }

    /**
     * @param period      Period / Hazard to compute
     * @param m           Moment Magnitude of the event
     * @param distance    Distance from the epicenter to the point of interest
     * @param focal_depth Depth of the event
     * @return Hazard Value
     * @todo We assume site class D, we must get this info from the user if
     * available
     */
    private double getValue(String period, double m, double distance, double focal_depth) {
        List<Double> coeff = getCoefficients(period);
        double r = Math.sqrt(Math.pow(distance, 2) + Math.pow(focal_depth, 2));
        // Constraint provided by Glenn Rix
        if (r < 10.0)
            r = 10.0;

        // System.out.println("R_hypo = "+r);
        double val = coeff.get(0) + coeff.get(1) * (m - 6) + coeff.get(2) * Math.pow((m - 6), 2) - Math.log10(r) - coeff.get(3) * r;

        // Site A motions with units of g
        val = Math.pow(10, val) * 0.001019716;

        // Convert to B/C
        val = HazardUtil.convertMotionsToBC(period, val);

        return val;

    }

}
