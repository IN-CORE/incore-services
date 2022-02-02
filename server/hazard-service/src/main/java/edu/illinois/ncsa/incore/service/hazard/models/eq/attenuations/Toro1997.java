package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Toro1997 extends BaseAttenuation {
    // Uncertainties for PGA, 0.2 Sa, 0.3 Sa, 1.0 Sa -> Lognormal values
    // Other uncertainties from Ergo
    private static final double[] aleatoric_uncertainties = {0.750, 0.750, 0.0, 0.80};
    private static final double[] aleatoric_uncertainties_mw_pga = {0.55, 0.59, 0.5};
    private static final double[] aleatoric_uncertainties_mw_02sa = {0.6, 0.64, 0.56};
    private static final double[] aleatoric_uncertainties_mw_03sa = {0.615, 0.66, 0.6};
    private static final double[] aleatoric_uncertainties_mw_10sa = {0.63, 0.64, 0.67};

    private static final double[] aleatoric_uncertainties_dist_pga = {0.54, 0.2};
    private static final double[] aleatoric_uncertainties_dist_02sa = {0.45, 0.12};
    private static final double[] aleatoric_uncertainties_dist_03sa = {0.45, 0.12};
    private static final double[] aleatoric_uncertainties_dist_10sa = {0.45, 0.12};

    public Toro1997() {
        InputStream is = Toro1997.class.getResourceAsStream("/hazard/earthquake/coefficients/Toro1997.csv");
        readCoefficients(is);
    }

    public double getValue(String period, Site site) throws Exception {
        double mag = ruptureParameters.getMagnitude();// double m =
        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        double distance = HazardUtil.findDistance(site.getLocation(), sourceSite.getLocation());
        return getValue(period, mag, distance);
    }

    public double getValue(String period, double m, double distance) {
        // Constraint provided by Glenn Rix
        if (distance < 1.0)
            distance = 1.0;
        List<Double> coeff = getCoefficients(period);

        double r_m = Math.sqrt(Math.pow(distance, 2) + Math.pow(coeff.get(6), 2));

        double val =
            coeff.get(0) + coeff.get(1) * (m - 6.0) + coeff.get(2) * Math.pow(m - 6.0, 2) - coeff.get(3) * Math.log(r_m) - (coeff.get(4) -
                coeff.get(3)) * Math.max(Math.log(r_m / 100.0), 0.0) - coeff.get(5) * r_m;
        // Site A motions
        val = Math.exp(val);

        // Convert to B/C
        val = HazardUtil.convertMotionsToBC(period, val);

        return val;
    }

    /**
     * @return
     */
    @Override
    public boolean canProduceStandardDeviation() {
        // TODO Auto-generated method stub
        return false;
    }

    public Map<String, Double> getAleatoricUncertainties() {
        return new HashMap<String, Double>() {
            {
                put("PGA", 0.75);
                put("0.2 SA", 0.75);
                put("0.3 SA", 0.0);
                put("1.0 SA", 0.80);
            }
        };
    }

    /**
     * @param median_hazard
     * @param period
     * @param site
     * @return Normal value of standard deviation
     * @throws Exception
     */
    public double getStandardDeviation(double median_hazard, String period, Site site) throws Exception {
        double std_deviation = 0.0;
        std_deviation = Math.sqrt(Math.pow(getAleatoricUncertainties(period), 2) + getEpistemicVariance(median_hazard, period, site));
        return std_deviation;
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
    public boolean isDipAngleRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isFaultTypeRequired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGeologyRequired() {
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
    public boolean isShearWaveDepthRequired() {
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

    /**
     * @return
     */
    @Override
    public boolean isShearWaveDepth10Required() {
        // TODO Auto-generated method stub
        return false;
    }

}
