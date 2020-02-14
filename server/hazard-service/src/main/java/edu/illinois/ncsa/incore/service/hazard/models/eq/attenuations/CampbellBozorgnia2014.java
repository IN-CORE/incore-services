/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd (NCSA) - initial API and implementation
 * Chris Navarro (NCSA) - migrated to version 2
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.EqRupture;
import edu.illinois.ncsa.incore.service.hazard.models.eq.FaultMechanism;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.DistanceUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class CampbellBozorgnia2014 extends BaseAttenuation {
    private static final Logger logger = Logger.getLogger(CampbellBozorgnia2014.class);

    private double n = 1.18;
    private double C = 1.18;
    private double[] c = new double[21]; // c coefficients
    private double[] k = new double[3]; // k coefficients
    private double[] h = new double[6]; // h coefficients
    private double a2; // a2 coefficient
    private double deltaC20; // regional c coefficient
    private double regionalCA, regionalJI, regionalCH; // regional coefficients for California, Japan-Italy, Eastern China
    private double phi1, phi2, tau1, tau2, phiLnAF, phiC, rhoLnPGA_LnY; // Aleatory Variability Model coefficients
    private double tau1PGA, tau2PGA, phi1PGA, phi2PGA;

    private String region;

    private double Vs30, Sa1100;

    public CampbellBozorgnia2014() {
        URL coefficientURL = CampbellBozorgnia2014.class.getResource("/hazard/earthquake/coefficients/CampbellBozorgnia2014.csv");
        readCoefficients(coefficientURL);
    }

    public double getValue(String period, Site site) throws Exception
    {
        // Median predicated value of PGA on rock with Vs30 = 1100 m/sec [g] (rock PGA)
        Sa1100 = getSa1100(site);

        double y = calculateCBAttenuation(period, site);

        // y < pga, T < 0.25 -> pga Otherwise return y
        if (NumberUtils.isNumber(period)) {
            double T = Double.parseDouble(period);

            if (T < 0.25) {
                double pga = calculateCBAttenuation("PGA", site);
                if (y < pga) {
                    return pga;
                }
            }
        }

        return y;
    }

    public double calculateCBAttenuation(String period, Site site)
    {
        loadCoefficients(period);
        EqRupture rupture = EqRupture.getInstance();

        region = ruptureParameters.getRegion();
        double azimuthAngle = ruptureParameters.getAzimuthAngle();
        String mechanism = ruptureParameters.getFaultType(this.getClass().getSimpleName());
        // TODO remove this
        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        double[] originalDistances = HazardUtil.computeOriginalDistance(site, sourceSite);
        // ****************************GET INPUTS********************************************
        // Moment magnitude
        double M = ruptureParameters.getMagnitude();

        // The average angle of slip measured in the plane of rupture between the strike direction and the slip vector
        double rakeAngle = ruptureParameters.getRakeAngle();

        double ruptureLength = rupture.getSubsurfaceRuptureLength(M, mechanism);
        double ruptureWidth = rupture.getDowndipRuptureWidth(M, mechanism);

        // The average dip of the rupture plane
        double dipAngle = ruptureParameters.getDipAngle();

        double[] transformedDistances = DistanceUtil.computeTransformedDistance(azimuthAngle, dipAngle, originalDistances);

        // The closest distance to the coseismic rupture plane [km]
        double Rrup = DistanceUtil.computeDistanceToRupturePlane(transformedDistances, ruptureWidth, ruptureLength);

        // The closest distance to the surface projection of the coseismic rupture plane [km] (Joyner-Boore distance)
        double Rjb = DistanceUtil.computeJoynerBooreDistance(transformedDistances, ruptureWidth, ruptureLength, dipAngle);

        // Time-averaged shear-wave velocity in the top 30m of the site [m/sec]
        Vs30 = site.getVs30();

        // Depth to the 2.5 km/sec shear-wave velocity horizon beneath the site [km] (sediment depth)
        double Z2p5 = ruptureParameters.getDepth2p5KmPerSecShearWaveVelocity();

        // Indicator representing regional site effects where Sj = 1 for sites located in Japan and Sj = 0 otherwise
        double Sj = region.equalsIgnoreCase("Japan") ? 1 : 0;

        // The depth to the top of the coseismic rupture plane [km]
        double Ztor = ruptureParameters.getCoseismicRuptureDepth();

        // Down-dip width of the rupture plane [km]
        double W = rupture.getDowndipRuptureWidth(M, mechanism);

        // The depth of the bottom of the rupture plane [km]
        double Zbor = Ztor + W * Math.sin(Math.toRadians(dipAngle));

        // Hypocentral depth of the earthquake [km]
        double Zhyp = getHypocentralDepth(M, dipAngle, Ztor, Zbor);

        // Closest distance to the surface projection of the top edge of the coseismic rupture plane measured perpendicular to its
        // average strike [km]
        double Rx = DistanceUtil.computeRx(Rjb, Ztor, W, dipAngle, azimuthAngle, Rrup);

        // ******************************COMPUTE*********************************************
        double fMag = calculateMagnitudeTerm(M);
        double fDis = calculateGeometricAttentuationTerm(M, Rrup);
        double fFlt = calculateStyleOfFaultingTerm(M, rakeAngle);
        double fHng = calculateHangingWallTerm(M, W, Rrup, dipAngle, Rx, Rjb, Ztor);
        double fSite = calculateShallowSiteResponseTerm(Vs30, Sj);
        double fSed = calculateBasinResponseTerm(Z2p5, Sj);
        double fHyp = calculateHypocentralDepthTerm(M, Zhyp);
        double fDip = calculateRuptureDipTerm(M, dipAngle);
        double fAtn = calculateAnelasticAttentuationTerm(Rrup);

        double y = Math.exp(fMag + fDis + fFlt + fHng + fSite + fSed + fHyp + fDip + fAtn);

        return y;
    }

    private void loadCoefficients(String period)
    {
        List<Double> periodCoefficients = getCoefficients(period);
        List<Double> pgaCoefficients = getCoefficients(HazardUtil.PGA);

        // C0 to C20
        for (int i = 0; i <= 20; i++) {
            c[i] = periodCoefficients.get(i);
        }

        regionalCA = periodCoefficients.get(21);
        regionalJI = periodCoefficients.get(22);
        regionalCH = periodCoefficients.get(23);

        // H1 to H6
        for (int i = 0; i < 6; i++) {
            h[i] = periodCoefficients.get(i+25);
        }

        // K1 to K3
        for (int i = 0; i < 3; i++) {
            k[i] = periodCoefficients.get(i+31);
        }

        C = periodCoefficients.get(34);
        n = periodCoefficients.get(35);

        phi1 = periodCoefficients.get(36);
        phi1PGA = pgaCoefficients.get(36);
        phi2 = periodCoefficients.get(37);
        phi2PGA = pgaCoefficients.get(37);

        tau1 = periodCoefficients.get(38);
        tau1PGA = pgaCoefficients.get(38);
        tau2 = periodCoefficients.get(39);
        tau2PGA = pgaCoefficients.get(39);

        phiLnAF = periodCoefficients.get(40);
        phiC = periodCoefficients.get(41);

        rhoLnPGA_LnY = periodCoefficients.get(42);
    }

    private double calculateMagnitudeTerm(double M)
    {
        if (M <= 4.5) {
            return c[0] + (c[1] * M);
        } else if (M > 4.5 && M <= 5.5) {
            return c[0] + (c[1] * M) + (c[2] * (M - 4.5));
        } else if (M > 5.5 && M <= 6.5) {
            return c[0] + (c[1] * M) + (c[2] * (M - 4.5)) + (c[3] * (M - 5.5));
        } else {
            return c[0] + (c[1] * M) + (c[2] * (M - 4.5)) + (c[3] * (M - 5.5)) + (c[4] * (M - 6.5));
        }
    }

    private double calculateGeometricAttentuationTerm(double magnitude, double rRup)
    {
        double fDis = (c[5] + (c[6] * magnitude)) * Math.log(Math.sqrt((rRup * rRup) + (c[7] * c[7])));

        return fDis;
    }

    private double calculateStyleOfFaultingTerm(double magnitude, double rakeAngle)
    {
        // Calculate fFlt,M
        double fFltM = 0.0;

        if (magnitude <= 4.5) {
            fFltM = 0.0;
        } else if (magnitude > 4.5 && magnitude <= 5.5) {
            fFltM = magnitude - 4.5;
        } else {
            fFltM = 1.0;
        }

        // Indicator variable representing reverse and reverse-oblique faulting
        double Frv = HazardUtil.getReverseFaultingFactorFlag(rakeAngle);
        // Indicator variable representing normal and normal-oblique faulting
        double Fnm = HazardUtil.getNormalFaultingFactorFlag(rakeAngle);

        // Calculate fFlt,F
        double fFltF = (c[8] * Frv) + (c[9] * Fnm);

        double fFlt = fFltF * fFltM;

        return fFlt;
    }

    private double calculateHangingWallTerm(double M, double W, double Rrup, double dipAngle, double Rx, double Rjb, double Ztor)
    {
        double fHngRx = 0;

        double R1 = W * Math.cos(Math.toRadians(dipAngle));
        double R2 = 62 * M - 350;

        // fHng,Rx
        if (Rx < 0) {
            fHngRx = 0;
        } else if (Rx >= 0 && Rx < R1) {
            double f1Rx = h[0] + (h[1] * (Rx / R1)) + h[2] * (Rx / (R1 * R1));
            fHngRx = f1Rx;
        } else {
            double f2Rx = h[3] + h[4] * ((Rx - R1) / (R2 - R1)) + h[5] * (Math.pow(((Rx - R1) / (R2 - R1)), 2));
            fHngRx = Math.max(f2Rx, 0);
        }

        // fHng,Rrup (Distance component)
        double fHngRrup = 1;
        if (Rrup > 0) {
            fHngRrup = (Rrup - Rjb) / Rrup;
        }

        // fHng,M (Magnitude component)
        double fHngM = 0;
        if (M <= 5.5) {
            fHngM = 0;
        } else if (M > 5.5 && M <= 6.5) {
            fHngM = (M - 5.5) * (1 + a2 * (M - 6.5));
        } else {
            fHngM = 1 + a2 * (M - 6.5);
        }

        // fHng,Z (Depth to the top of the co-seismic rupture component)
        double fHngZ = 0;
        if (Ztor <= 16.66) {
            fHngZ = 1 - (0.06 * Ztor);
        }

        // fHng,Dip (Dip angle Component)
        double fHngDip = (90 - dipAngle) / 45;

        double fHng = c[10] * fHngRx * fHngRrup * fHngM * fHngZ * fHngDip;

        return fHng;
    }

    private double getSa1100(Site site)
    {
        double originalVs30 = site.getVs30();

        try {
            site.setVs30(1100);
            Sa1100 = this.calculateCBAttenuation("PGA", site); //$NON-NLS-1$
            site.setVs30(originalVs30);
        } catch (Exception e) {
            logger.debug("Unable to compute A_1100, the value of pga on rock with Vs = 1100 m/s"); //$NON-NLS-1$
        }

        return Sa1100;
    }

    private double calculateShallowSiteResponseTerm(double Vs30, double Sj)
    {
        // if k[PGD] >= 1180 this can cause issues

        double fSiteG, fSiteJ;

        // fSite,G
        if (Vs30 <= k[0]) {
            fSiteG = c[11] * Math.log(Vs30 / k[0]) + k[1] * (Math.log(Sa1100 + C * Math.pow(Vs30 / k[0], n)) - Math.log(Sa1100 + C));
        } else {
            fSiteG = (c[11] + (k[1] * n)) * Math.log(Vs30 / k[0]);
        }

        // fSite,J
        if (Vs30 <= 200) {
            fSiteJ = (c[12] + (k[1] * n)) * (Math.log(Vs30 / k[0]) - Math.log(200 / k[0]));
        } else { // All Vs30 ?
            fSiteJ = (c[13] + (k[1] * n)) * Math.log(Vs30 / k[0]);
        }

        double fSite = fSiteG + (Sj * fSiteJ);

        return fSite;
    }

    private double calculateBasinResponseTerm(double Z2p5, double Sj)
    {
        double fSed = 0.0;

        if (Z2p5 < 1.0) {
            fSed = (c[14] + c[15] * Sj) * (Z2p5 - 1.0);
        } else if (Z2p5 >= 1.0 && Z2p5 <= 3.0) {
            fSed = 0.0;
        } else if (Z2p5 > 3.0) {
            fSed = (c[16] * k[2] * Math.exp(-0.75)) * (1 - Math.exp(-0.25 * (Z2p5 - 3.0)));
        }

        return fSed;
    }

    private double calculateHypocentralDepthTerm(double M, double Zhyp)
    {
        double fHypH = 0;
        double fHypM = c[17];

        // fHyp,H
        if (Zhyp <= 7) {
            fHypH = 0;
        } else if (Zhyp > 7 && Zhyp <= 20) {
            fHypH = Zhyp - 7;
        } else {
            fHypH = 13;
        }

        // fHyp,M
        if (M <= 5.5) {
            fHypM = c[17];
        } else if (M > 5.5 && M <= 6.5) {
            fHypM = c[17] + ((c[18] - c[17]) * (M - 5.5));
        } else {
            fHypM = c[18];
        }

        double fHyp = fHypH * fHypM;

        return fHyp;
    }

    private double calculateRuptureDipTerm(double M, double dipAngle)
    {
        double fDip = 0;

        if (M <= 4.5) {
            fDip = c[19] * dipAngle;
        } else if (M > 4.5 && M <= 5.5) {
            fDip = c[19] * ((5.5 - M) * dipAngle);
        } else {
            fDip = 0;
        }

        return fDip;
    }

    private double calculateAnelasticAttentuationTerm(double Rrup)
    {
        double fAtn = 0;

        if (region.equalsIgnoreCase("Japan") || region.equals("Italy")) { //$NON-NLS-1$ //$NON-NLS-2$
            deltaC20 = regionalJI;
        } else if (region.equalsIgnoreCase("China") || region.equals("Turkey")) { //$NON-NLS-1$ //$NON-NLS-2$
            deltaC20 = regionalCH;
        } else {
            deltaC20 = regionalCA;
        }

        if (Rrup > 80) {
            fAtn = (c[20] + deltaC20) * (Rrup - 80);
        } else {
            fAtn = 0;
        }

        return fAtn;
    }

    public Map<String, Double> getAleatoricUncertainties() {
        return null;
    }

    public double getAleatoricStdDev(String period, Site site) throws Exception
    {
        return 0;
    }

    
    public double getStandardDeviation(double median_hazard, String period, Site site) throws Exception
    {
        // This will load up the required values such as Sa1100 and Vs30
        double y = getValue(period, site);

        // Moment of magnitude
        double M = ruptureParameters.getMagnitude();

        double tauLnY, phiLnY;
        double tauLnPGA, phiLnPGA; // Paper makes no mention of how these are computed, but copied from the Matlab code

        if (M <= 4.5) {
            tauLnY = tau1;
            phiLnY = phi1;
            tauLnPGA = tau1PGA;
            phiLnPGA = phi1PGA;
        } else if (M > 4.5 && M < 5.5) {
            tauLnY = tau2 + (tau1 - tau2) * (5.5 - M);
            phiLnY = phi2 + (phi1 - phi2) * (5.5 - M);
            tauLnPGA = tau2PGA + (tau1PGA - tau2PGA) * (5.5 - M);
            phiLnPGA = phi2PGA + (phi1PGA - phi2PGA) * (5.5 - M);
        } else {
            tauLnY = tau2;
            phiLnY = phi2;
            tauLnPGA = tau2PGA;
            phiLnPGA = phi2PGA;
        }

        double alpha;
        if (Vs30 < k[1]) {
            alpha = k[2] * Sa1100 * Math.pow(Sa1100 + C * Math.pow(Vs30 / k[1], n), -1) - Math.pow(Sa1100 + C, -1);
        } else {
            alpha = 0;
        }

        double tauLnYb = tauLnY;
        double tauLnPGAb = tauLnPGA;
        double phiLnYb = Math.sqrt(square(phiLnY) - square(phiLnAF));
        double phiLnPGAb = Math.sqrt(square(phiLnPGA) - square(phiLnAF));

        double tau = Math.sqrt(square(tauLnYb) + square(alpha) + square(tauLnPGAb) + 2 * alpha * rhoLnPGA_LnY * tauLnYb * tauLnPGAb);

        double phi = Math.sqrt(
            square(phiLnYb) + square(phiLnAF) + square(alpha) * square(phiLnPGAb) + 2 * alpha * rhoLnPGA_LnY * phiLnYb * phiLnPGAb);

        double sigma = Math.sqrt(square(tau) + square(phi));

        return sigma;
    }

    // Convenience method, consider moving to BaseAttenuation if it's used elsewhere
    private double square(double x)
    {
        return Math.pow(x, 2);
    }

    private double getHypocentralDepth(double M, double dip, double Ztor, double Zbor)
    {
        double fDeltaZ_M, fDeltaZ_Dip;

        if (M < 6.75) {
            fDeltaZ_M = -4.317 + 0.984 * M;
        } else {
            fDeltaZ_M = 2.325;
        }

        if (dip <= 40) {
            fDeltaZ_Dip = 0.0445 * (dip - 40);
        } else {
            fDeltaZ_Dip = 0;
        }

        double deltaZ = Math.exp(Math.min(fDeltaZ_M + fDeltaZ_Dip, Math.log(0.9 * (Zbor - Ztor))));

        double Zhyp = deltaZ + Ztor;

        return Zhyp;
    }

    
    public boolean isRegionRequired()
    {
        return true;
    }

    
    public String[] getRegions()
    {
        return new String[] { "California", "Japan", "China", "Turkey", "Italy" };
    };

    
    public boolean canProduceStandardDeviation()
    {
        return false;
    }

    
    public String[] getFaultMechanisms()
    {
        return new String[] { FaultMechanism.STRIKE_SLIP, FaultMechanism.REVERSE, FaultMechanism.NORMAL };
    }

    
    public boolean isFaultTypeRequired()
    {
        return true;
    }

    
    public boolean isGeologyRequired()
    {
        return false;
    }

    
    public boolean isDipAngleRequired()
    {
        return true;
    }

    
    public boolean isAzimuthAngleRequired()
    {
        return true;
    }

    
    public boolean isCoseismicRuptureDepthRequired()
    {
        return true;
    }

    
    public boolean isShearWaveDepthRequired()
    {
        return true;
    }

    
    public boolean isShearWaveDepth10Required()
    {
        return false;
    }

    
    public boolean isRakeAngleRequired()
    {
        return true;
    }

    
    public boolean isSeismogenicDepthRequired()
    {
        return false;
    }

    
    public boolean isSoilMapRequired()
    {
        return false;
    }

    
    public boolean isSoilTypeRequired()
    {
        return true;
    }
}
