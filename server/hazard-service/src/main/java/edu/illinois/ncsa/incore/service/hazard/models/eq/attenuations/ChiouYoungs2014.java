package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.EqRupture;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.DistanceUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.EqUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import org.apache.commons.lang.math.NumberUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ChiouYoungs2014 extends BaseAttenuation {
    private double[] c = new double[12];
    private double c4a, cRB, c1a, c1b, c1c, c1d, cn, cM, cHM, c7b, c8a, c8b, c9a, c9b, c11b, cg1, cg2, cg3;

    private double phi1, phi2, phi3, phi4, phi5, phi6;
    private double tau1, tau2;
    private double sigma1, s2, sigma3;

    private double sigma2_JP;
    private double phi1_JP, phi5_JP, phi6_JP;
    private double gamma_JP_IT; // Constant for Japan, Italy
    private double gamma_Wn;

    private String region;
    // private boolean isVs30Inferred;

    private double Vs30, M, yref;

    public ChiouYoungs2014() {
        InputStream coefficientURL = AtkinsonBoore1995.class.getResourceAsStream("/hazard/earthquake/coefficients/ChiouYoungs2014.csv");
        readCoefficients(coefficientURL);
    }

    //TODO: Find if we have aleatoric uncertainties defined in the paper
    public Map<String, Double> getAleatoricUncertainties() {
        return null;
    }

    public double getValue(String period, Site site) throws Exception {
        double y = calculateCYAttenuation(period, site);
        if (NumberUtils.isNumber(period)) {
            double T = Double.parseDouble(period);

            if (T <= 0.3) {
                double pga = calculateCYAttenuation("PGA", site);
                if (y <= pga) {
                    return pga;
                }
            }
        }

        return y;
    }

    public double calculateCYAttenuation(String period, Site site) {
        // Load coefficients into local variables
        loadCoefficients(period);
        region = ruptureParameters.getRegion();

        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        EqRupture rupture = EqRupture.getInstance();
        double azimuthAngle = ruptureParameters.getAzimuthAngle();
        String mechanism = ruptureParameters.getFaultType(this.getClass().getSimpleName());
        double[] originalDistances = HazardUtil.computeOriginalDistance(site, sourceSite);

        // Moment magnitude
        M = ruptureParameters.getMagnitude();

        double ruptureLength = rupture.getSubsurfaceRuptureLength(M, mechanism);
        double ruptureWidth = rupture.getDowndipRuptureWidth(M, mechanism);

        // The average dip of the rupture plane
        double dipAngle = ruptureParameters.getDipAngle();
        double[] transformedDistances = DistanceUtil.computeTransformedDistance(azimuthAngle, dipAngle, originalDistances);

        // The closest distance to the coseismic rupture plane [km]
        double Rrup = DistanceUtil.computeDistanceToRupturePlane(transformedDistances, ruptureWidth, ruptureLength);

        // The closest distance to the surface projection of the coseismic rupture plane [km] (Joyner-Boore distance)
        double Rjb = DistanceUtil.computeJoynerBooreDistance(transformedDistances, ruptureWidth, ruptureLength, dipAngle);

        // The average angle of slip measured in the plane of rupture between the strike direction and the slip vector
        double rakeAngle = ruptureParameters.getRakeAngle();

        // Time-averaged shear-wave velocity in the top 30m of the site [m/sec]
        Vs30 = site.getVs30();

        // Depth to Vs=1.0 km/s at the site [km]
        double Z1p0 = ruptureParameters.getShearWaveDepth1p0();

        // The depth to the top of the coseismic rupture plane [km]
        double Ztor = ruptureParameters.getCoseismicRuptureDepth();

        // Down-dip width of the rupture plane [km]
        double W = rupture.getDowndipRuptureWidth(M, mechanism);

        // Closest distance to the surface projection of the top edge of the coseismic rupture plane measured perpendicular
        // to its average strike [km]
        double Rx = DistanceUtil.computeRx(Rjb, Ztor, W, dipAngle, azimuthAngle, Rrup);

        // Indicator variable representing reverse and reverse-oblique faulting
        double Frv = EqUtil.getReverseFaultingFactorFlag(rakeAngle);
        // Indicator variable representing normal and normal-oblique faulting
        double Fnm = EqUtil.getNormalFaultingFactorFlag(rakeAngle);

        // Hanging-wall flag
        double Fhw = (Rx >= 0) ? 1 : 0;

        double Z1p0Regional;
        if (region.equalsIgnoreCase("Japan")) {
            Z1p0Regional = calculateZ1p0RegionalJapan(Vs30);
        } else {
            Z1p0Regional = calculateZ1p0RegionalCalifornia(Vs30);
        }

        double deltaZ1p0 = Z1p0 - Z1p0Regional;
        double E_Ztor;
        // Reverse Faulting
        if (Frv == 1) {
            E_Ztor = Math.pow(Math.max(2.704 - 1.226 * Math.max(M - 5.849, 0), 0), 2);
        } else { // Reverse Oblique Faulting
            E_Ztor = Math.pow(Math.max(2.673 - 1.136 * Math.max(M - 4.970, 0), 0), 2);
        }

        double deltaZtor = Ztor - E_Ztor;

        double deltaDPP = 0;
        if (region.equalsIgnoreCase("Japan")) {
            phi1 = phi1_JP;
            phi5 = phi5_JP;
            phi6 = phi6_JP;
        }

        // ******************************COMPUTE*********************************************
        yref = Math.exp(c[1] + fFlt_1(M, Frv) + fFlt_2(M, Fnm) + fTor(M, deltaZtor) + fDip(M, dipAngle) + fMag(M) + fDis(M, Rrup)
            + fAtn(M, Rrup) + fDir(M, Rrup, deltaDPP) + fHng(Fhw, dipAngle, Rx, Rjb, Ztor, Rrup));

        // yref * X instead of Ln(yref) + X because e^(ln(x)+y) = x * e^y
        double y = yref * Math.exp(phi1 * Math.min(Math.log(Vs30 / 1130), 0)
            + phi2 * (Math.exp(phi3 * (Math.min(Vs30, 1130) - 360) - Math.exp(phi3 * (1130 - 360)))) * Math.log((yref + phi4) / phi4)
            + phi5 * (1 - Math.exp(-deltaZ1p0 / phi6)) /* + n + e */);

        return y;
    }

    private void loadCoefficients(String period) {
        // Get coefficients for period, then pull them out
        List<Double> periodCoefficients = getCoefficients(period);

        c[2] = periodCoefficients.get(0);
        c[4] = periodCoefficients.get(1);
        c4a = periodCoefficients.get(2);
        cRB = periodCoefficients.get(3);
        c[8] = periodCoefficients.get(4);
        c8a = periodCoefficients.get(5);
        c[1] = periodCoefficients.get(6);
        c1a = periodCoefficients.get(7);
        c1b = periodCoefficients.get(8);
        c1c = periodCoefficients.get(9);
        c1d = periodCoefficients.get(10);
        cn = periodCoefficients.get(11);
        cM = periodCoefficients.get(12);
        c[3] = periodCoefficients.get(13);
        c[5] = periodCoefficients.get(14);
        cHM = periodCoefficients.get(15);
        c[6] = periodCoefficients.get(16);
        c[7] = periodCoefficients.get(17);
        c7b = periodCoefficients.get(18);
        c8b = periodCoefficients.get(19);
        c[9] = periodCoefficients.get(20);
        c9a = periodCoefficients.get(21);
        c9b = periodCoefficients.get(22);
        c[11] = periodCoefficients.get(23);
        c11b = periodCoefficients.get(24);
        cg1 = periodCoefficients.get(25);
        cg2 = periodCoefficients.get(26);
        cg3 = periodCoefficients.get(27);

        // Standard Deviation
        phi1 = periodCoefficients.get(28);
        phi2 = periodCoefficients.get(29);
        phi3 = periodCoefficients.get(30);
        phi4 = periodCoefficients.get(31);
        phi5 = periodCoefficients.get(32);
        phi6 = periodCoefficients.get(33);
        tau1 = periodCoefficients.get(34);
        tau2 = periodCoefficients.get(35);
        sigma1 = periodCoefficients.get(36);
        s2 = periodCoefficients.get(37);
        sigma3 = periodCoefficients.get(38);

        // Regional Coefficients
        sigma2_JP = periodCoefficients.get(39);
        gamma_JP_IT = periodCoefficients.get(40);
        gamma_Wn = periodCoefficients.get(41);
        phi1_JP = periodCoefficients.get(42);
        phi5_JP = periodCoefficients.get(43);
        phi6_JP = periodCoefficients.get(44);
    }

    // Style of Faulting Term
    private double fFlt_1(double M, double Frv) {
        return (c1a + (c1c / Math.cosh(2 * Math.max(M - 4.5, 0.0)))) * Frv;
    }

    // Style of Faulting Term
    private double fFlt_2(double M, double Fnm) {
        return (c1b + (c1d / Math.cosh(2 * Math.max(M - 4.5, 0.0)))) * Fnm;
    }

    // Ztor Term
    private double fTor(double M, double deltaZtor) {
        return (c[7] + (c7b / Math.cosh(2 * Math.max(M - 4.5, 0.0)))) * deltaZtor;
    }

    // Dip Term
    private double fDip(double M, double dip) {
        return (c[11] + (c11b / Math.cosh(2 * Math.max(M - 4.5, 0.0)))) * Math.pow(Math.cos(Math.toRadians(dip)), 2);
    }

    // Magnitude Scaling Term
    private double fMag(double M) {
        return c[2] * (M - 6) + ((c[2] - c[3]) / cn) * Math.log(1 + (Math.exp(cn * (cM - M))));
    }

    // Distance Attentuation Term
    private double fDis(double M, double Rrup) {
        return c[4] * Math.log(Rrup + c[5] * Math.cosh(c[6] * Math.max(M - cHM, 0)))
            + (c4a - c[4]) * Math.log(Math.sqrt(square(Rrup) + square(cRB)));
    }

    // Distance Attentuation Term
    private double fAtn(double M, double Rrup) {
        double fAtn = (cg1 + (cg2 / (Math.cosh(Math.max(M - cg3, 0))))) * Rrup;

        if (region.equalsIgnoreCase("Japan") || region.equalsIgnoreCase("Italy")) {  //$NON-NLS-2$
            if (M > 6 && M < 6.9) {
                fAtn = gamma_JP_IT * fAtn;
            }
        }

        if (region.equalsIgnoreCase("China")) {
            fAtn = gamma_Wn * fAtn;
        }

        return fAtn;
    }

    // Directivity Term
    private double fDir(double M, double Rrup, double deltaDPP) {
        return c[8] * Math.max(1 - (Math.max(Rrup - 40, 0) / 30.0), 0) * Math.min(Math.max(M - 5.5, 0) / 0.8, 1)
            * Math.exp(-c8a * square(M - c8b)) * deltaDPP;
    }

    // Hanging Wall Term
    private double fHng(double Fhw, double dip, double Rx, double Rjb, double Ztor, double Rrup) {
        return c[9] * Fhw * Math.cos(Math.toRadians(dip)) * (c9a + (1 - c9a) * Math.tanh(Rx / c9b))
            * (1 - (Math.sqrt(square(Rjb) + square(Ztor)) / (Rrup + 1)));
    }

    private double calculateZ1p0RegionalCalifornia(double Vs30) {
        return Math.exp((-7.15 / 4.0) * Math.log((Math.pow(Vs30, 4) + Math.pow(571, 4)) / Math.pow(1360, 4) + Math.pow(571, 4)));
    }

    private double calculateZ1p0RegionalJapan(double Vs30) {
        return Math.exp((-5.23 / 2.0) * Math.log((Math.pow(Vs30, 2) + Math.pow(412, 2)) / Math.pow(1360, 2) + Math.pow(412, 2)));
    }

    public double square(double x) {
        return Math.pow(x, 2);
    }

    @Override
    public double getStandardDeviation(double median_hazard, String period, Site site) throws Exception {
        // TODO add option to GUI if needed
        double Finferred = 1;
        double Fmeasured = 0;

        double sigma2;
        if (region.equalsIgnoreCase("Japan")) {
            sigma2 = sigma2_JP;
        } else {
            sigma2 = s2;
        }

        double tau = tau1 + ((tau2 - tau1) / 1.5) * (Math.min(Math.max(M, 5), 6.5) - 5);

        double NL0 = phi2 * Math.exp(phi3 * Math.min(Vs30, 1130) - 360) * Math.exp(phi3 * (1130 - 360)) * (yref / (yref + phi4));
        double sigmaNL0 = (sigma1 + ((sigma2 - sigma1) / 1.5)) * (Math.min(Math.max(M, 5), 6.5) - 5)
            * Math.sqrt(sigma3 * Finferred + 0.7 * Fmeasured + square(1 + NL0));

        double sigma = Math.sqrt(square(1 + NL0) * square(tau) + square(sigmaNL0));

        return sigma;
    }

    public boolean canProduceStandardDeviation() {
        return true;
    }

    @Override
    public String[] getRegions() {
        return new String[]{"California", "Japan", "China", "Turkey", "Italy"};  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    @Override
    public boolean isRegionRequired() {
        return true;
    }

    @Override
    public boolean isFaultTypeRequired() {
        return true;
    }

    @Override
    public boolean isGeologyRequired() {
        return false;
    }

    @Override
    public boolean isDipAngleRequired() {
        return true;
    }

    @Override
    public boolean isAzimuthAngleRequired() {
        return true;
    }

    @Override
    public boolean isCoseismicRuptureDepthRequired() {
        return true;
    }

    @Override
    public boolean isShearWaveDepthRequired() {
        return false;
    }

    @Override
    public boolean isShearWaveDepth10Required() {
        return true;
    }

    @Override
    public boolean isRakeAngleRequired() {
        return true;
    }

    @Override
    public boolean isSeismogenicDepthRequired() {
        return false;
    }

    @Override
    public boolean isSoilMapRequired() {
        return false;
    }

    @Override
    public boolean isSoilTypeRequired() {
        return true;
    }

    // TODO - consider adding this in some form where we can return these messages to the user
//    @Override
//    public ValidationResult isMagnitudeRangeValid(double M, String region, String faultType)
//    {
//        if (faultType.equals(FaultMechanism.STRIKE_SLIP)) {
//            if (M >= 3.5 && M <= 8.5) {
//                return ValidationResult.getTrueResult();
//            } else {
//                return new ValidationResult(false, "Magnitude must be between 3.5 and 8.5 for strike-slip earthquakes");
//            }
//        } else {
//            if (M >= 3.5 && M <= 8.0) {
//                return ValidationResult.getTrueResult();
//            } else {
//                return new ValidationResult(false, "Magnitude must be between 3.5 and 8.0 for normal and reverse faulting earthquakes");
//            }
//        }
//    }
//
//    @Override
//    public ValidationResult isCoseismicRuptureDepthRangeValid(double Ztor)
//    {
//        if (Ztor <= 20) {
//            return ValidationResult.getTrueResult();
//        } else {
//            return new ValidationResult(false, "Coseismic rupture depth must be less than or equal to 20 km.");
//        }
//    }
}
