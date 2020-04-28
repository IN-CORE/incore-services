/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class AbrahamsonSilvaKamai2014 extends BaseAttenuation {
    private static final Logger logger = Logger.getLogger(AbrahamsonSilvaKamai2014.class);
    private double[] a = new double[47];
    private double c4;
    private double n;
    private double b;
    private double c;
    private double VLin;
    private double[] s = new double[5];
    private double M1;
    private double M2 = 5.0;
    private double Sa1180 = -999;
    private double Rrup;
    private double Ftw, Fcn, Fjp; // Regional flags for Taiwan, China and Japan
    private double s5_JP, s6_JP;
    // These are not used - the paper should be reviewed to see if they are useful or should be removed
    private double s1_measured, s2_measured;
    private String region;

    public AbrahamsonSilvaKamai2014() {
        InputStream coefficientURL = AtkinsonBoore1995.class.getResourceAsStream("/hazard/earthquake/coefficients/AbrahamsonSilvaKamai2014.csv");
        readCoefficients(coefficientURL);
    }

    public double getValue(String period, Site site) throws Exception {
        Sa1180 = getSa1180(site); // Recursive Call
        double Sa = calculateASKAttenuation(period, site);
        return Sa;
    }

    public double calculateASKAttenuation(String period, Site site) {
        // Region for using regionalization coefficients
        region = ruptureParameters.getRegion();
        loadCoefficients(period);

        EqRupture rupture = EqRupture.getInstance();
        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        double azimuthAngle = ruptureParameters.getAzimuthAngle();
        double rakeAngle = ruptureParameters.getRakeAngle();
        String mechanism = ruptureParameters.getFaultType(this.getClass().getSimpleName());
        double[] originalDistances = HazardUtil.computeOriginalDistance(site, sourceSite);

        // Model inputs
        // Moment magnitude
        double M = ruptureParameters.getMagnitude();

        double ruptureLength = rupture.getSubsurfaceRuptureLength(M, mechanism);
        double ruptureWidth = rupture.getDowndipRuptureWidth(M, mechanism);

        // The depth to the top of the coseismic rupture plane [km]
        double Ztor = ruptureParameters.getCoseismicRuptureDepth();

        // The average dip of the rupture plane
        double dip = ruptureParameters.getDipAngle();

        // Time-averaged shear-wave velocity in the top 30m of the site [m/sec]
        double Vs30 = site.getVs30();

        // Depth to Vs=1.0 km/s at the site [km]
        double Z1p0 = ruptureParameters.getShearWaveDepth1p0();

        // Indicator variable representing reverse and reverse-oblique faulting
        double Frv = HazardUtil.getReverseFaultingFactorFlag(rakeAngle);
        // Indicator variable representing normal and normal-oblique faulting
        double Fn = HazardUtil.getNormalFaultingFactorFlag(rakeAngle);

        double[] transformedDistances = DistanceUtil.computeTransformedDistance(azimuthAngle, dip, originalDistances);

        // The closest distance to the coseismic rupture plane [km]
        Rrup = DistanceUtil.computeDistanceToRupturePlane(transformedDistances, ruptureWidth, ruptureLength);

        // The closest distance to the surface projection of the coseismic rupture plane [km] (Joyner-Boore distance)
        double Rjb = DistanceUtil.computeJoynerBooreDistance(transformedDistances, ruptureWidth, ruptureLength, dip);

        // Down-dip width of the rupture plane [km]
        double W = rupture.getDowndipRuptureWidth(M, mechanism);

        // Closest distance to the surface projection of the top edge of the coseismic rupture plane measured perpendicular to its
        // average strike [km]
        double Rx = DistanceUtil.computeRx(Rjb, Ztor, W, dip, azimuthAngle, Rrup);

        // Hanging-wall flag
        double Fhw = (Rx >= 0) ? 1 : 0;

        // Flag for aftershocks
        double Fas = 1;

        double T;
        if (NumberUtils.isNumber(period)) {
            T = Double.parseDouble(period);
        } else { // PGA, PGV
            T = -1;
        }

        // Centroid Joyner-Boore distance
        double CRjb = 999.0;

        // Compute ground motion
        double Sa = Math.exp(f1(M, Rrup) + Frv * f7(M) + Fn * f8(M) + Fas * f11(CRjb) + f5(T, Vs30)
            + Fhw * f4(Rjb, Rrup, Rx, dip, Ztor, M, W) + f6(Ztor) + f10(Z1p0, Vs30) + Regional(Vs30, Rrup));

        return Sa;
    }

    /**
     * The base form of the magnitude and distance dependence for strike-slip earthquakes
     *
     * @param M    Moment of Magnitude
     * @param Rrup Closest distance to the coseismic rupture plane (km)
     * @return The base model
     */
    public double f1(double M, double Rrup) {
        double f1;

        double c4M;
        if (M > 5.0) {
            c4M = c4;
        } else if (M > 4 && M <= 5.0) {
            c4M = c4 - (c4 - 1.0) * (5.0 - M);
        } else {
            c4M = 1.0;
        }

        double R = Math.sqrt(square(Rrup) + square(c4M));

        if (M > M1) {
            f1 = a[1] + a[5] * (M - M1) + (a[8] * square(8.5 - M)) + (a[2] + a[3] * (M - M1)) * Math.log(R) + a[17] * Rrup;
        } else if (M >= M2 && M < M1) {
            f1 = a[1] + a[4] * (M - M1) + (a[8] * square(8.5 - M)) + (a[2] + a[3] * (M - M1)) * Math.log(R) + a[17] * Rrup;
        } else if (M < M2) {
            f1 = a[1] + a[4] * (M - M1) + (a[8] * square(8.5 - M)) + (a[6] * (M - M1)) + (a[7] * square(M - M2))
                + (a[2] + a[3] * (M - M1)) * Math.log(R) + a[17] * Rrup;
        } else {
            // Should only occur if coefficients were not setup properly (M1 > M2)
            logger.debug("An error occured, caused by an invalid state in the software, please contact ergo-user@illinois.edu ");
            throw new IllegalStateException("The specified coefficients resulted in an invalid execution state where M1 > M2");
        }

        return f1;
    }

    /**
     * Computes the Style-of-faulting model
     *
     * @param M Moment of magnitude
     * @return The Style-of-faulting model
     */
    public double f7(double M) {
        if (M > 5.0) {
            return a[11];
        } else if (M >= 4.0 && M <= 5.0) {
            return a[11] * (M - 4.0);
        } else {
            return 0.0;
        }
    }

    /**
     * Computes the Style-of-faulting model
     *
     * @param M Moment of magnitude
     * @return Returns the Style-of-faulting model
     */
    public double f8(double M) {
        if (M > 5.0) {
            return a[12];
        } else if (M >= 4.0 && M <= 5.0) {
            return a[12] * (M - 4.0);
        } else {
            return 0.0;
        }
    }

    /**
     * Computes the Site Response Model
     *
     * @param Vs30 Time-averaged shear-wave velocity in the top 30m of the site [m/sec]
     * @return Site Response Model
     */
    public double f5(double T, double Vs30) {
        double f5, V1;

        // Calculate V1
        if (T <= 0.5) {
            V1 = 1500;
        } else if (T > 0.5 && T < 3.0) {
            V1 = Math.exp(-0.35 * Math.log(T / 0.5) + Math.log(1500));
        } else {
            V1 = 800;
        }

        // Calculate (Vs*)30
        double Vs30_Star = Vs30;
        if (Vs30 >= V1) {
            Vs30_Star = V1;
        }

        if (Vs30 >= VLin) {
            f5 = (a[10] + b * n) * Math.log(Vs30_Star / VLin);
        } else {
            f5 = (a[10] * Math.log(Vs30_Star / VLin))
                - (b * Math.log(Sa1180 + c) + b * Math.log(Sa1180 + c * Math.pow((Vs30_Star / VLin), n)));
        }

        return f5;
    }

    /**
     * Computes the Hanging Wall Model
     *
     * @param Rjb  Joyner-Boore distance(km)
     * @param Rrup Rupture distance (km)
     * @param Rx   Horizontal distance (km) from top edge of rupture measured perpendicular to the fault strike
     * @param dip  Fault dip in degrees
     * @param Ztor Depth-to-top of rupture (km)
     * @param M    Moment magnitude
     * @param W    Down-dip width of the rupture plane (km)
     * @return Hanging Wall Model
     */
    public double f4(double Rjb, double Rrup, double Rx, double dip, double Ztor, double M, double W) {
        double T1Taper, T2Taper, T3Taper, T4Taper, T5Taper;
        double R1 = W * Math.cos(Math.toRadians(dip));
        double R2 = 3 * R1;
        double h1 = 0.25;
        double h2 = 1.5;
        double h3 = -0.75;

        double a2HW = 0.2;

        // T1(dip)
        if (dip > 30.0) {
            T1Taper = (90.0 - dip) / 45.0;
        } else { // TODO dip == 30 not defined
            T1Taper = 60.0 / 45.0;
        }

        // T2(M)
        if (M >= 6.5) {
            T2Taper = 1.0 + a2HW * (M - 6.5);
        } else if (M > 5.5 && M < 6.5) {
            T2Taper = 1.0 + a2HW * (M - 6.5) - (1 - a2HW) * square(M - 6.5);
        } else {
            T2Taper = 0;
        }

        // T3(Rx)
        if (Rx < R1) {
            T3Taper = h1 + h2 * (Rx / R1) + h3 * square(Rx / R1);
        } else if (Rx > R1 && Rx <= R2) {
            T3Taper = 1.0 - ((Rx - R1) / (R2 - R1));
        } else if (Rx > R2) {
            T3Taper = 0.0;
        } else {
            // Should only occur if coefficients were not setup properly (R1 > R2)
            logger.debug(
                "An error occured, caused by an invalid state in the software, please contact ergo-user@ncsa.illinois.edu for further assistance");
            throw new IllegalStateException("The specified coefficients resulted in an invalid execution state where M1 > M2");
        }

        // T4(Ztor)
        if (Ztor < 10.0) {
            T4Taper = 1.0 - (square(Ztor) / 100.0);
        } else {
            // Although Ztor == 10.0 was not specified in the paper, the T4Taper will = 0 in either case.
            T4Taper = 0.0;
        }

        // double Ry1 = Rx * Math.tan(20.0);

        // T5(Rjb) - if Ry0 is not available
        if (Rjb == 0) {
            T5Taper = 1;
        } else if (Rjb < 30) {
            T5Taper = 1 - (Rjb / 30);
        } else {
            T5Taper = 0;
        }

        return a[13] * T1Taper * T2Taper * T3Taper * T4Taper * T5Taper;
    }

    /**
     * Computes the Depth-to-Top of Rupture Model
     *
     * @param Ztor Depth-to-top of rupture (km)
     * @return Depth-to-Top of the Rupture Model
     */
    public double f6(double Ztor) {
        if (Ztor < 20.0) {
            return a[15] * (Ztor / 20.0);
        } else {
            return a[15];
        }
    }

    /**
     * Computes the Soil Depth Model (EQ 2/10/2014.17)
     *
     * @param Z1p0 Depth to Vs = 1.0 km/s at the site (km)
     * @param Vs30 Shear-wave velocity over the top 30 m (m/s)
     * @return Soil Depth Model
     */
    public double f10(double Z1p0, double Vs30) {
        double Z1ref;

        if (region.equalsIgnoreCase("Japan")) {
            Z1ref = (1.0 / 1000.0)
                * Math.exp((-5.23 / 2.0) * Math.log((Math.pow(Vs30, 2) + Math.pow(412, 2)) / (Math.pow(1360, 2) + Math.pow(412, 2))));
        } else {
            Z1ref = (1.0 / 1000.0)
                * Math.exp((-7.67 / 4.0) * Math.log((Math.pow(Vs30, 4) + Math.pow(610, 4)) / (Math.pow(1360, 4) + Math.pow(610, 4))));
        }

        double y1z, y2z, x1z, x2z;

        if (Vs30 <= 150.0) {
            y1z = a[43];
            y2z = a[43];
            x1z = 50;
            x2z = 150;
        } else if (Vs30 > 150.0 && Vs30 <= 250.0) {
            y1z = a[43];
            y2z = a[44];
            x1z = 150;
            x2z = 250;
        } else if (Vs30 > 250.0 && Vs30 <= 400.0) {
            y1z = a[44];
            y2z = a[45];
            x1z = 250;
            x2z = 400;
        } else if (Vs30 > 400 && Vs30 <= 700.0) {
            y1z = a[45];
            y2z = a[46];
            x1z = 400;
            x2z = 700;
        } else {
            y1z = a[46];
            y2z = a[46];
            x1z = 700;
            x2z = 1000;
        }

        return (y1z + (Vs30 - x1z) * (y2z - y1z) / (x2z - x1z)) * Math.log((Z1p0 + 0.01) / (Z1ref + 0.01));
    }

    /**
     * Computes the Aftershock Scaling
     *
     * @param CRjb Centroid Joyner-Boore distance, the shortest distance between the centroid of the surface projection of
     *             the rupture surface of the potential Class 2 earthquakes and the surface projection of the
     *             rupture surface of the main shock (see Wooddell and Abrahamson 2014)
     * @return Returns the Aftershock scaling
     */
    public double f11(double CRjb) {
        if (CRjb <= 5.0) {
            return a[14];
        } else if (CRjb > 5.0 && CRjb < 15.0) {
            return a[14] * (1 - ((CRjb - 5) / 10.0));
        } else {
            return 0.0;
        }
    }

    /**
     * Computes the regionalization of the Vs30 Scaling
     *
     * @param Vs30 Shear-wave velocity over the top 30 m (m/s)
     * @param Rrup Rupture distance (km)
     * @return
     */
    public double Regional(double Vs30, double Rrup) {
        return Ftw * (f12(Vs30) + a[25] * Rrup) + Fcn * (a[28] * Rrup) + Fjp * (f13(Vs30) + a[29] * Rrup);
    }

    public double f12(double Vs30) {
        return a[31] * Math.log(Vs30 / VLin);
    }

    /**
     * Regionalization Term for Vs30 Scaling
     *
     * @param Vs30 Shear-wave velocity over the top 30 m (m/s)
     * @return Scaled Vs30
     */
    public double f13(double Vs30) {
        double x1, x2, y1, y2;

        if (Vs30 < 150.0) {
            y1 = a[36];
            y2 = a[36];
            x1 = 50;
            x2 = 150;
        } else if (Vs30 < 250.0) {
            y1 = a[36];
            y2 = a[37];
            x1 = 150;
            x2 = 250;
        } else if (Vs30 < 350.0) {
            y1 = a[37];
            y2 = a[38];
            x1 = 250;
            x2 = 350;
        } else if (Vs30 < 450.0) {
            y1 = a[38];
            y2 = a[39];
            x1 = 350;
            x2 = 450;
        } else if (Vs30 < 600.0) {
            y1 = a[39];
            y2 = a[40];
            x1 = 450;
            x2 = 600;
        } else if (Vs30 < 850.0) {
            y1 = a[40];
            y2 = a[41];
            x1 = 600;
            x2 = 850;
        } else if (Vs30 < 1150.0) {
            y1 = a[41];
            y2 = a[42];
            x1 = 850;
            x2 = 1150;
        } else {
            y1 = a[42];
            y2 = a[43];
            x1 = 1150;
            x2 = 3000;
        }

        return y1 + (y2 - y1) / (x2 - x1) * (Vs30 - x1);
    }

    private void loadCoefficients(String period) {
        List<Double> periodCoefficients = getCoefficients(period);

        VLin = periodCoefficients.get(0);
        b = periodCoefficients.get(1);
        n = periodCoefficients.get(2);
        M1 = periodCoefficients.get(3);
        c = periodCoefficients.get(4);
        c4 = periodCoefficients.get(5);

        // a1 to a8
        for (int i = 1; i <= 8; i++) {
            a[i] = periodCoefficients.get(i + 5);

        }
        // a10 to a17
        for (int i = 10; i <= 17; i++) {
            a[i] = periodCoefficients.get(i + 4);
        }

        a[25] = periodCoefficients.get(26);
        a[28] = periodCoefficients.get(27);
        a[29] = periodCoefficients.get(28);
        a[31] = periodCoefficients.get(29);

        // a36 to a42
        for (int i = 36; i <= 42; i++) {
            a[i] = periodCoefficients.get(i - 6);
        }

        // a43 to a46
        for (int i = 43; i <= 46; i++) {
            a[i] = periodCoefficients.get(i - 21);
        }
        // s1 to s4
        for (int i = 1; i <= 4; i++) {
            s[i] = periodCoefficients.get(i + 36);
        }

        s5_JP = periodCoefficients.get(43);
        s6_JP = periodCoefficients.get(44);

        Ftw = 0;
        Fcn = 0;
        Fjp = 0;
        if (region.equalsIgnoreCase("Japan")) {
            Fjp = 1;
        } else if (region.equalsIgnoreCase("China")) {
            Fcn = 1;
        } else if (region.equalsIgnoreCase("Taiwan")) {
            Ftw = 1;
        }

        s1_measured = periodCoefficients.get(41);
        s2_measured = periodCoefficients.get(42);
    }

    public double getStandardDeviation(double median_hazard, String period, Site site) throws Exception {
        // get value to load everything
        getValue(period, site);

        // if region is Japan
        if (region.equalsIgnoreCase("Japan")) {
            return getStandardDeviation_Japan();
        } else {
            // Moment magnitude
            double M = ruptureParameters.getMagnitude();
            // Time-averaged shear-wave velocity in the top 30m of the site [m/sec]
            double Vs30 = site.getVs30();

            double phiAL, tauAL;
            double phiAmp = 0.4;
            double s1, s2;

            // if (isVs30Measured()) {
            // s1 = s1_measured;
            // s2 = s2_measured;
            // } else {
            s1 = s[1];
            s2 = s[2];
            // }

            if (M < 4.0) {
                phiAL = s1;
            } else if (M >= 4 && M <= 6) {
                phiAL = s1 + ((s2 - s1) / 2) * (M - 4.0);
            } else {
                phiAL = s2;
            }

            if (M < 5.0) {
                tauAL = s[3];
            } else if (M >= 5.0 && M <= 7.0) {
                tauAL = s[3] + ((s[4] - s[3]) / 2.0) * (M - 5.0);
            } else {
                tauAL = s[4];
            }

            double phiB = Math.sqrt(square(phiAL) - square(phiAmp));
            double tauB = tauAL;

            double dln;
            if (Vs30 >= VLin) {
                dln = 0;
            } else {
                dln = (-b * Sa1180 / (Sa1180 + c)) + ((b * Sa1180) / (Sa1180 + c * Math.pow(Vs30 / VLin, n)));
            }

            double phi = Math.sqrt(square(phiB) * square(1 + dln) + square(phiAmp));

            double tau = tauB * (1 + dln);

            return Math.sqrt(square(phi) + square(tau));
        }
    }

    public Map<String, Double> getAleatoricUncertainties() {
        return null;
    }

    private double getStandardDeviation_Japan() {
        double phiAL_JP;
        if (Rrup < 30.0) {
            phiAL_JP = s5_JP;
        } else if (Rrup >= 30.0 && Rrup <= 80.0) {
            phiAL_JP = (s5_JP + ((s6_JP - s5_JP) / 50.0) * (Rrup - 30.0));
        } else {
            phiAL_JP = s6_JP;
        }

        return phiAL_JP;
    }

    private double getSa1180(Site site) {
        double originalVs30 = site.getVs30();

        try {
            site.setVs30(1180);
            Sa1180 = this.calculateASKAttenuation("PGA", site);
            site.setVs30(originalVs30);
        } catch (Exception e) {
            logger.debug("Unable to compute A_1180, the value of pga on rock with Vs = 1100 m/s");
        }

        return Sa1180;
    }

    // Convenience method, consider moving to BaseAttenuation if it's used elsewhere
    private double square(double x)
    {
        return Math.pow(x, 2);
    }

    public boolean canProduceStandardDeviation() {
        return false;
    }

    public boolean isFaultTypeRequired() {
        return false;
    }

    public String[] getFaultMechanisms() {
        return new String[]{FaultMechanism.STRIKE_SLIP, FaultMechanism.REVERSE, FaultMechanism.NORMAL};
    }

    public boolean isRegionRequired() {
        return true;
    }

    public String[] getRegions() {
        return new String[]{"California", "Japan", "China", "Taiwan"};  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public boolean isGeologyRequired() {
        return false;
    }

    public boolean isDipAngleRequired() {
        return true;
    }

    public boolean isAzimuthAngleRequired() {
        return true;
    }

    public boolean isCoseismicRuptureDepthRequired() {
        return true;
    }

    public boolean isShearWaveDepthRequired() {
        return false;
    }

    public boolean isShearWaveDepth10Required() {
        return true;
    }

    public boolean isRakeAngleRequired() {
        return true;
    }

    public boolean isSeismogenicDepthRequired() {
        return false;
    }

    public boolean isSoilMapRequired() {
        return false;
    }

    public boolean isSoilTypeRequired() {
        return false;
    }

    // TODO add validation
//    public ValidationResult isMagnitudeRangeValid(double M, String region, String faultType)
//    {
//        if (M >= 3.0 && M <= 8.5) {
//            return ValidationResult.getTrueResult();
//        } else {
//            return new ValidationResult(false, "Magnitude must be between 3.0 and 8.5");
//        }
//    }
//
//    public ValidationResult isFocalDepthValid(double focalDepth)
//    {
//        if (focalDepth >= 0 && focalDepth <= 300) {
//            return ValidationResult.getTrueResult();
//        } else {
//            return new ValidationResult(false, "Focal depth must be between 0 and 300 km");
//        }
//    }
}
