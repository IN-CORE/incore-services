package edu.illinois.ncsa.incore.services.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.services.hazard.models.eq.EqParameters;
import edu.illinois.ncsa.incore.services.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.services.hazard.models.eq.types.SeismicHazardResult;
import edu.illinois.ncsa.incore.services.hazard.models.eq.utils.HazardUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public abstract class BaseAttenuation {

    private static final Logger logger = Logger.getLogger(BaseAttenuation.class);
    protected EqParameters ruptureParameters;
    protected List<String> hazardOutputTypes;
    protected Map<String, List<Double>> coefficients;

    /**
     * Subclasses should implement this method and put required model parameters
     * in a map
     *
     * @return hazard value
     */
    public abstract double getValue(String period, Site site) throws Exception;

    /**
     *
     * @param median_hazard
     * @param period
     * @param site
     * @return
     * @throws Exception
     */
    public abstract double getStandardDeviation(double median_hazard, String period, Site site) throws Exception;

    /**
     *
     * @param period
     * @param site
     * @return
     * @throws Exception
     */
    public abstract double getAleatoricStdDev(String period, Site site) throws Exception;

    /**
     *
     * @param medianHazard
     * @param period
     * @param site
     * @return
     * @throws Exception
     */
    public double getEpistemicVariance(double medianHazard, String period, Site site) throws Exception
    {
        double hazard = getValue(period, site);
        double epistemicVariance = Math.pow(Math.log(hazard) - Math.log(medianHazard), 2);
        return epistemicVariance;
    }

    public void readCoeffients(URL fileURL) {
        System.out.println("read coefficients");
        hazardOutputTypes = new LinkedList<String>();
        coefficients = new HashMap<String, List<Double>>();

        try {
            File coefficientFile = new File(fileURL.toURI());
            CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();

            try (Reader csvFileReader = new FileReader(coefficientFile)) {
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

                    coefficients.put(period, coeff);

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
                logger.error("Could not read cofficient file for attenuation.", e);
            }
        } catch(URISyntaxException e) {
            logger.error("Error parsing coefficients file for attenuation", e);
        }


    }

    /**
     * Determines if the attenuation can output this desired hazardType
     *
     * @param hazardType
     * @return true/false whether it can be supported for output
     */
    public boolean canOutput(String hazardType)
    {
        if (hazardType == null) {
            return false;
        }

        // quick hack to work around the fact that canOutput expects hazardTypes
        // of Sa to be expressed in the form "0.2 Sa", but other parts of
        // are
        // happy with "0.2"...if we match (numbers).(numbers), then tack the
        // " Sa"
        // on at the end.
        if (hazardType.matches("[0-9]*\\.[0-9]*")) { //$NON-NLS-1$
            hazardType = hazardType + " Sa"; //$NON-NLS-1$
        }
        hazardType = hazardType.replaceFirst(" sec", ""); //$NON-NLS-1$ //$NON-NLS-2$
        List<String> outputs = getHazardOutputTypes();
        for (String output : outputs) {
            if (hazardType.toLowerCase().equals(output.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to parse any sort of hazard type ("0.2", "0.2 sec Sa", "PGA", etc)
     * and figure out it's period. If it is non-numeric (PGA), then will return
     * NaN;
     *
     * @param hazardType
     * @return the period as a double
     */
    private double periodFromHazard(String hazardType)
    {
        double period = 0.0;
        String[] split = hazardType.split(" "); //$NON-NLS-1$
        if (split.length > 0) {
            String periodStr = split[0];
            try {
                period = Double.parseDouble(periodStr);
            } catch (NumberFormatException e) {
                period = Double.NaN;
            }
        }
        return period;
    }


    /**
     * Gets the closest supported hazard to the desired one.<br>
     * If the hazard is exactly supported, just returns it.<br>
     * If not, find the supported hazard of the correct type with the closest
     * period, and returns its string representation.<br>
     * Returns <b>Null</b> if this hazardType isn't supported at all.
     *
     * @param hazardType
     * @return the string representation of the closest supported hazard.
     */
    public String closestSupportedHazard(String hazardType)
    {
        double period = periodFromHazard(hazardType);

        String closest = null;
        double closestAbs = Double.NaN;

        List<String> outputs = getHazardOutputTypes();
        for (String output : outputs) {

            // if they are actually the same, then return 0.0, meaning we
            // found a perfect match
            if (output.equalsIgnoreCase(hazardType)) {
                return hazardType;
            }
            double tryPeriod = periodFromHazard(output);

            // if one of them doesn't have a period, then we can't use this one
            // (the only match with a non-period type is the exact type, which
            // we tried
            // above)
            if (!(Double.isNaN(period) || (Double.isNaN(tryPeriod)))) {
                double distance = Math.abs(period - tryPeriod);

                // if this is the first possible match, or the closest possible
                // match,
                // save it
                if (Double.isNaN(closestAbs) || (distance < closestAbs)) {
                    closest = output;
                    closestAbs = distance;
                }
            }

        }
        return closest;
    }

    public SeismicHazardResult getValueClosestMatch(String hazardType, Site site) throws Exception
    {
        // if we exact match, don't bother running through them all
        if (canOutput(hazardType)) {
            return new SeismicHazardResult(getValue(hazardType, site), hazardType);
        }

        // find the closest period, and use it...
        double period = HazardUtil.getPeriod(hazardType);
        String motionType = HazardUtil.stripPeriod(hazardType);

        String closest = null;
        double distance = 9999999;

        List<String> outputs = getHazardOutputTypes();
        for (String output : outputs) {
            String outputMotionType = HazardUtil.stripPeriod(output);
            if (outputMotionType.equalsIgnoreCase(motionType)) {

                double outputPeriod = HazardUtil.getPeriod(output);
                double outputDistance = Math.abs(outputPeriod - period);
                if (outputDistance < distance) {
                    closest = Double.toString(outputPeriod);
                    distance = outputDistance;
                }
            }
        }

        // will get here if the ground motion types never match...
        if (closest == null) {
            throw new Exception("Unsupported hazard");
        }

        return new SeismicHazardResult(getValue(closest, site), closest);
    }

    public static String getUnits(String hazardType)
    {
        if (hazardType.equalsIgnoreCase(HazardUtil.PGA)) {
            return "g"; //$NON-NLS-1$
        } else if (hazardType.equalsIgnoreCase(HazardUtil.PGD)) {
            return "m"; //$NON-NLS-1$
        } else if (hazardType.equalsIgnoreCase(HazardUtil.PGV)) {
            return "cm/s"; //$NON-NLS-1$
        } else if (hazardType.equalsIgnoreCase("SA")) { //$NON-NLS-1$
            return "g"; //$NON-NLS-1$
        } else if (hazardType.equalsIgnoreCase("SD")) { //$NON-NLS-1$
            return "cm"; //$NON-NLS-1$
        } else if (hazardType.equalsIgnoreCase("SV")) { //$NON-NLS-1$
            return "cm/s"; //$NON-NLS-1$
        } else {
            return "g"; //$NON-NLS-1$
        }
    }

    /**
     *
     * @return
     */
    public abstract boolean canProduceStandardDeviation();

    public EqParameters getRuptureParameters()
    {
        return ruptureParameters;
    }

    public void setRuptureParameters(EqParameters ruptureParameters)
    {
        this.ruptureParameters = ruptureParameters;
    }

    /**
     *
     * @param period
     * @return
     */
    protected List<Double> getCoefficients(String period)
    {
        return coefficients.get(period);
    }

    /**
     *
     * @return List of hazard outputs
     */
    public List<String> getHazardOutputTypes()
    {
        return hazardOutputTypes;
    }

    public String[] getRegions()
    {
        return new String[] { "Global" };
    }
}
