package edu.illinois.ncsa.incore.service.hazard.models.eq;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EqRupture {
    private static final Logger log = Logger.getLogger(EqRupture.class);

    private static EqRupture instance;
    private List<CSVRecord> coefficientRecords;

    /**
     *
     */
    protected EqRupture() {
        coefficientRecords = new LinkedList<CSVRecord>();
        URL coefficientURL = EqRupture.class.getResource("/hazard/earthquake/RuptureRegressionCoefficients.csv");
        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        File coefficientFile = null;
        try {
            coefficientFile = new File(coefficientURL.toURI());
            try (Reader csvFileReader = new FileReader(coefficientFile)) {
                CSVParser csvParser = new CSVParser(csvFileReader, csvFormat);
                Iterator<CSVRecord> csvIterator = csvParser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvLine = csvIterator.next();
                    coefficientRecords.add(csvLine);
                }
            } catch (FileNotFoundException e) {
                log.error("Could not find rupture regression coefficients.", e);
            } catch (IOException e) {
                log.error("Error reading rupture regression coefficients.", e);
            }
        } catch (URISyntaxException e) {
            log.error("Could not find rupture regression coefficients.", e);
        }
    }

    /**
     * @return
     */
    public static EqRupture getInstance() {
        if (instance == null) {
            instance = new EqRupture();
        }

        return instance;
    }

    public List<CSVRecord> getCoefficientRecords() {
        return this.coefficientRecords;
    }

    /**
     * @param magnitude Magnitude of the earthquake
     * @param mechanism Fault Mechanism (Strike-Slip, Reverse, Normal, All)
     * @return Rupture area (km<sup>2</sup>)
     */
    public double getRuptureArea(double magnitude, String mechanism) {
        String dimension = "Area"; //$NON-NLS-1$
        double[] coefficients = getFaultMechanismCoefficients(mechanism, dimension);

        double a = coefficients[0];
        double b = coefficients[1];

        return Math.pow(10, a + b * magnitude);
    }

    /**
     * @param magnitude Magnitude of the earthquake
     * @param mechanism Fault Mechanism (Strike-Slip, Reverse, Normal, All)
     * @return Surface rupture length (km)
     */
    public double getSurfaceRuptureLength(double magnitude, String mechanism) {
        String dimension = "surfaceLength"; //$NON-NLS-1$
        double[] coefficients = getFaultMechanismCoefficients(mechanism, dimension);

        double a = coefficients[0];
        double b = coefficients[1];

        return Math.pow(10, a + b * magnitude);
    }

    /**
     * @param magnitude Magnitude of the earthquake
     * @param mechanism Fault Mechanism (Strike-Slip, Reverse, Normal, All)
     * @return Subsurface rupture length (km)
     */
    public double getSubsurfaceRuptureLength(double magnitude, String mechanism) {
        String dimension = "Length"; //$NON-NLS-1$
        double[] coefficients = getFaultMechanismCoefficients(mechanism, dimension);

        double a = coefficients[0];
        double b = coefficients[1];

        return Math.pow(10, a + b * magnitude);
    }

    /**
     * @param magnitude Magnitude of the earthquake
     * @param mechanism Fault Mechanism (Strike-Slip, Reverse, Normal, All)
     * @return Down-dip rupture width of the rupture (km)
     */
    public double getDowndipRuptureWidth(double magnitude, String mechanism) {
        String dimension = "Width"; //$NON-NLS-1$
        double[] coefficients = getFaultMechanismCoefficients(mechanism, dimension);

        double a = coefficients[0];
        double b = coefficients[1];

        return Math.pow(10, a + b * magnitude);
    }

    /**
     * @param mechanism
     * @param dimension
     * @return
     */
    private static double[] getFaultMechanismCoefficients(String mechanism, String dimension) {
        double[] coefficients = {0.0, 0.0};

        String convertedMechanism = mechanism;
        if (mechanism.startsWith("Reverse") || mechanism.startsWith("Interface") || mechanism.startsWith("Intraslab")) {
            convertedMechanism = FaultMechanism.REVERSE;
        } else if (mechanism.startsWith("Normal")) {
            convertedMechanism = FaultMechanism.NORMAL;
        }

        List<CSVRecord> coeff = getInstance().getCoefficientRecords();
        for (int row = 0; row < coeff.size(); row++) {
            CSVRecord record = coeff.get(row);
            String rowMechanism = record.get(0);
            if (convertedMechanism.equalsIgnoreCase(rowMechanism)) {
                String rowDimension = record.get(1);
                if (rowDimension.equalsIgnoreCase(dimension)) {
                    coefficients[0] = Double.parseDouble(record.get(2));
                    coefficients[1] = Double.parseDouble(record.get(3));
                    return coefficients;
                }
            }
        }

        return coefficients;
    }
}
