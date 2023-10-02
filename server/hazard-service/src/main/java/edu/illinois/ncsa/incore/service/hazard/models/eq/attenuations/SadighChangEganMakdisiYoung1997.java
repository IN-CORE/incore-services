package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.EqRupture;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.DistanceUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

public class SadighChangEganMakdisiYoung1997 extends BaseAttenuation {
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    // This model has separate coefficients for small earthquakes and the coefficient method expects one set per demand type
    private Map<String, List<Double>> smallEqCoefficients;

    public SadighChangEganMakdisiYoung1997() {
        InputStream is = SadighChangEganMakdisiYoung1997.class.getResourceAsStream("/hazard/earthquake/coefficients" +
            "/SadighChangEganMakdisiYoung1997Coefficients.csv");
        readCoefficients(is);
    }

    /**
     * Get hazard for requested location and demand type
     *
     * @param period
     * @param site
     * @return
     * @throws Exception
     */
    public double getValue(String period, Site site) throws Exception {
        String mechanism = "Strike-Slip";
        mechanism = ruptureParameters.getFaultType(this.getClass().getSimpleName());

        double mag = ruptureParameters.getMagnitude();
        int soilType = findSoilType(site);

        EqRupture rupture = EqRupture.getInstance();
        double ruptureLength = rupture.getSubsurfaceRuptureLength(mag, mechanism);
        double ruptureWidth = rupture.getDowndipRuptureWidth(mag, mechanism);

        double dipAngle = ruptureParameters.getDipAngle();
        double azimuthAngle = ruptureParameters.getAzimuthAngle();

        Site sourceSite = HazardUtil.getSourceSite(ruptureParameters);

        double[] originalDistances = HazardUtil.computeOriginalDistance(site, sourceSite);
        double[] transformedDistances = DistanceUtil.computeTransformedDistance(azimuthAngle, dipAngle, originalDistances);
        double r_rup = DistanceUtil.computeDistanceToRupturePlane(transformedDistances, ruptureWidth, ruptureLength);

        double value = getValue(period, mag, r_rup, mechanism, soilType);
        return value;
    }

    /**
     * @param period    Period of the deterministic event
     * @param m         Moment Magnitude of the deterministic event
     * @param r_rup     Distance from the site to the fault rupture plane
     * @param mechanism strike slip earthquake, reverse slip earthquake, normal
     * @param soilType  0 - Rock, 1 -Deep Soil
     * @return
     */
    private double getValue(String period, double m, double r_rup, String mechanism, int soilType) {
        List<Double> coeff = getCoefficients(period, m);

        double val = 0.0;
        if (soilType == 0) {
            val = coeff.get(0) + coeff.get(1) * m + coeff.get(2) * Math.pow((8.5 - m), 2.5) + coeff.get(3) * Math.log(r_rup +
                Math.exp(coeff.get(4) + coeff.get(5) * m)) + coeff.get(6) * Math.log(r_rup + 2);
        } else {
            val = coeff.get(0) + coeff.get(1) * m - coeff.get(2) * Math.log(r_rup + coeff.get(3) * Math.exp(coeff.get(4) * m)) +
                coeff.get(5) + coeff.get(6) * Math.pow((8.5 - m), 2.5);
        }

        double f = this.getFaultFactor(mechanism);
        val += Math.log(f);

        return Math.exp(val);
    }

    public void readCoefficients(InputStream is) {
        // Override read coefficients since this model uses different coefficients based on magnitude
        // The first set of coefficients in the CSV file are for > 6.5 EQ and the second set is for <= 6.5
        hazardOutputTypes = new LinkedList<String>();
        coefficients = new HashMap<String, List<Double>>();
        smallEqCoefficients = new HashMap<String, List<Double>>();

        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();

        try (Reader csvFileReader = new InputStreamReader(is)) {
            CSVParser csvParser = new CSVParser(csvFileReader, csvFormat);
            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            List<Double> coeff = null;
            while (csvIterator.hasNext()) {
                CSVRecord csvLine = csvIterator.next();
                String period = csvLine.get(1);
                coeff = new LinkedList<Double>();
                for (int column = 2; column < csvLine.size(); column++) {
                    coeff.add(Double.parseDouble(csvLine.get(column)));
                }
                if (!coefficients.containsKey(period)) {
                    coefficients.put(period, coeff);
                } else {
                    smallEqCoefficients.put(period, coeff);
                }

                try {
                    // if we get an exception, we know that the period was PGA, PGV,
                    // or PGD and not an Sa value
                    Double.parseDouble(period);
                    period += " Sa"; //$NON-NLS-1$
                } catch (NumberFormatException nfe) {
                }

                if (!hazardOutputTypes.contains(period)) {
                    hazardOutputTypes.add(period);
                }

            }

        } catch (FileNotFoundException e) {
            logger.error("Could not find coefficient file for attenuation.", e);
        } catch (IOException e) {
            logger.error("Could not read coefficient file for attenuation.", e);
        }

    }

    protected List<Double> getCoefficients(String period, double magnitude) {
        if (magnitude > 6.5) {
            return getCoefficients(period);
        }

        if (smallEqCoefficients.containsKey(period)) {
            return smallEqCoefficients.get(period);
        } else {
            // Handles case differences for PGA, PGV, etc. This could be handled in the service as well
            if (smallEqCoefficients.containsKey(period.toUpperCase())) {
                return smallEqCoefficients.get(period.toUpperCase());
            } else if (smallEqCoefficients.containsKey(period.toLowerCase())) {
                return smallEqCoefficients.get(period.toLowerCase());
            }
        }
        return null;
    }

    /**
     * @param mechanism
     * @return
     */
    private double getFaultFactor(String mechanism) {
        // Relationships for reverse/thrust faulting are obtained by multiplying the strike-slip amplitudes by 1.2
        double f = 1.0;

        if (mechanism.startsWith("Reverse")) {
            f = 1.2;
        }

        return f;
    }

    /**
     * @param site
     * @return
     */
    private int findSoilType(Site site) {
        // Currently there is no algorithm to determine whether it is rock or deep soil based on the site class information
        // We assume rock for now
        int soilType = 0;
        return soilType;
    }

    /**
     * @return
     */
    public boolean canProduceStandardDeviation() {
        return true;
    }

    /**
     * @param median_hazard
     * @param period
     * @param site
     * @return
     * @throws Exception
     */
    public double getStandardDeviation(double median_hazard, String period, Site site) throws Exception {
        double std_deviation = 0.0;
        std_deviation = Math.sqrt(Math.pow(getAleatoricUncertainties(period), 2) +
            getEpistemicVariance(median_hazard, period, site));
        return std_deviation;
    }

    public Map<String, Double> getAleatoricUncertainties() {
        if (ruptureParameters != null) {
            double mag = ruptureParameters.getMagnitude();
            double multiplier = Math.min(mag, 7.21);
            return new HashMap<String, Double>() {
                {
                    put("PGA", 1.39 - 0.14 * multiplier);
                    put("0.2", 1.43 - 0.14 * multiplier);
                    put("0.3", 1.45 - 0.14 * multiplier);
                    put("1.0", 1.53 - 0.14 * multiplier);
                }
            };
        } else {
            return new HashMap<String, Double>() {
                {
                    put("PGA", 1.39);
                    put("0.2", 1.43);
                    put("0.3", 1.45);
                    put("1.0", 1.53);
                }
            };
        }
    }

    /**
     * @return
     */
    public boolean isFaultTypeRequired() {
        return true;
    }

    /**
     * @return
     */
    public boolean isAzimuthAngleRequired() {
        return true;
    }

    /**
     * @return
     */
    public boolean isCoseismicRuptureDepthRequired() {
        return false;
    }

    /**
     * @return
     */
    public boolean isDipAngleRequired() {
        return true;
    }

    /**
     * @return
     */
    public boolean isGeologyRequired() {
        return false;
    }

    /**
     * @return
     */
    public boolean isRakeAngleRequired() {
        return false;
    }

    /**
     * @return
     */
    public boolean isShearWaveDepthRequired() {
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
     * List of supported fault mechanisms
     *
     * @return List of supported fault mechanisms
     */
    public String[] getFaultMechanisms() {
        return new String[]{"Normal", "Strike-Slip", "Reverse"};
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
        return false;
    }

}
