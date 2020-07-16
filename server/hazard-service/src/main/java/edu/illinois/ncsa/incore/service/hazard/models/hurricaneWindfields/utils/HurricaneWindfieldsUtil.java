/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Gowtham Naraharisetty (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.utils;

import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneGrid;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneSimulationDataset;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Math.*;


/**
 * Misc utility functions for doing conversion of hazard types and units
 */
public class HurricaneWindfieldsUtil {
    public static final String HAZARD = "hurricane";
    public static final String UNITS_M = "m";
    public static final String UNITS_KT = "kt";
    public static final String UNITS_MPS = "mps";
    public static final String UNITS_KMPH = "kmph";
    public static final String UNITS_MPH = "mph";
    public static final double R_EARTH = 6371 * 1000; // mean radius of earth, meter
    public static final double PARA = 3.5;
    public static final double X_MAX = 6 * 80;
    public static final double RHO = 1.2754; // air density, kg/m^3
    public static final double MB2PA = 100; // unit change from mb to Pa
    public static final double PN = 1013.25 * MB2PA; // Pa, ambient pressure, 101.325 kPa, 29.921 inHg, 760 mmHg.

    public static final double KT2MS = 0.514444;
    public static final double KT2KMPH = 1.8519984;
    public static final double KT2MPH = 1.15078;

    public static final double MPH2MS = 0.44704;
    public static final double MPH2KMPH = 1.60934;
    public static final double MPH2KT = 0.868974;

    public static final double KMPH2MS = 0.277778;
    public static final double KMPH2MPH = 0.6213714;
    public static final double KMPH2KT = 0.539957;

    public static final double MS2KMPH = 3.6;
    public static final double MS2MPH = 2.23694;
    public static final double MS2KT = 1.94384;

    public static final double FR = 0.8; //Conversion parameter
    public static final int TOTAL_OMEGAS = 16; //This is 360 degrees divided by 22.5 degrees
    public static final Map<String, String[]> categoryMapping;
    private static final Logger log = Logger.getLogger(HurricaneWindfieldsUtil.class);
    public static List<Integer> OMEGAS_ALL = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    public static final String GRID_RESOLUTION_UNITS = "km";
    public static final String RASTER_RESOLUTION_UNITS = "km";
    public static final String TRANSD_UNITS = "degrees";
    public static final double WINDFIELD_DEFAULT_ELEVATION = 10.0;
    public static final double STANDARD_OPEN_TERRAIN_ROUGHNESS = 0.03;
    public static final double VAN_KORMAN_CONSTANT = 0.4;
    public static final String WIND_VELOCITY_3SECS = "3s";
    public static final String WIND_VELOCITY_60SECS = "60s";

    static {
        categoryMapping = new HashMap<String, String[]>();
        categoryMapping.put("gulf", new String[]{
            "Katrina", "Ike", "Katrina", "Harvey", ""
        });
        categoryMapping.put("florida", new String[]{
            "Katrina2", "Frances", "Wilma", "Irma", "Andrew"
        });
        categoryMapping.put("east", new String[]{
            "Sandy", "Isabel", "Fran", "", ""
        });
    }

    /**
     * Returns a complex number from a string.
     *
     * @param strComplex String representation of the complex number. ex: "2+i", "-2.1 - 3i", " -7i", "3.4", "1.7e4+340.9i"
     * @param imgChar    imaginary character in the string like i, j etc.
     * @return Complex number object with real and imaginary values set
     */
    public static Complex parseComplexString(String strComplex, char imgChar) {
        String rgxPattern = "^(?=[" + imgChar + ".\\d+-])([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?" +
            "(?![" + imgChar + ".\\d]))?([+-]?(?:(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)?[" + imgChar + "])?$";

        Pattern pattern = Pattern.compile(rgxPattern);
        Matcher matcher = pattern.matcher(strComplex.replace(" ", "")); // Eliminate spaces in the string

        String strImg = "";
        double real, img;
        real = img = 0;
        DecimalFormat df = new DecimalFormat();

        try {
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    real = df.parse(matcher.group(1)).doubleValue();
                }

                if (matcher.group(2) != null) {
                    strImg = matcher.group(2);
                    if (strImg.trim().charAt(0) == imgChar) {  // if imag part is just "i" return 1
                        img = 1;
                    } else { //repalce i from imaginary Part of the input
                        img = df.parse(strImg.replaceAll("[" + imgChar + "+]", "")).doubleValue();
                    }
                }
            }
        } catch (java.text.ParseException e) {
            System.out.println("Parse Error: " + strComplex);
        }

        return new Complex(real, img);
    }


    /**
     * @param arr
     * @return
     */
    public static final List<List<Complex>> convert2DComplexArrayToList(JSONArray arr) {

        List<List<Complex>> retList = new ArrayList<>();

        int rows = arr.size();

        for (int i = 0; i < rows; i++) {
            List<Complex> rowList = new ArrayList<>();
            List<Complex> row = (List<Complex>) arr.get(i);
            int cols = row.size();
            for (int j = 0; j < cols; j++) {
                Object col = row.get(j);

                if (col instanceof String) {
                    rowList.add(parseComplexString((String) col, 'j'));
                } else if (col instanceof Double) {
                    rowList.add(new Complex((Double) col, 0));
                } else if (col instanceof Complex) {
                    rowList.add((Complex) col);
                } else {
                    System.out.println("Unknown Type Detected"); // This should never happen
                }
            }
            retList.add(rowList);
        }
        return retList;
    }

    /***
     * Calculate abs value and apply 3-s gust conversion
     * @param cArr
     * @return
     */
    public static final List<List<Double>> convert2DComplexArrayToAbsList(Complex[][] cArr, String demandType, String demandUnits) {
        List<List<Double>> retList = new ArrayList<>();
        int rows = cArr.length;
        int cols = 0;
        if (cArr[0] != null) {
            cols = cArr[0].length;
        }

        for (int row = 0; row < rows; row++) {
            List<Double> rowList = new ArrayList<>();
            for (int col = 0; col < cols; col++) {
                //rowList.add(cArr[row][col].abs());
                double windVal = cArr[row][col].abs();
                if(!demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS)){
                    windVal = convertWindfieldVelocity(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS, windVal, WINDFIELD_DEFAULT_ELEVATION,
                        STANDARD_OPEN_TERRAIN_ROUGHNESS).get(demandType);
                }

                if(!demandUnits.equalsIgnoreCase(UNITS_KT)){
                    windVal = getCorrectUnitsOfVelocity(windVal, UNITS_KT, demandUnits);
                }

                rowList.add(windVal);
            }
            retList.add(rowList);
        }

        return retList;
    }


    public static final List<List<String>> convert2DComplexArrayToStringList(Complex[][] cArr, String demandType, String demandUnits) {
        List<List<String>> retList = new ArrayList<>();
        int rows = cArr.length;
        int cols = 0;
        if (cArr[0] != null) {
            cols = cArr[0].length;
        }

        double factor = 1.0;
        if(!demandType.equalsIgnoreCase(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS)){
            factor = convertWindfieldVelocity(HurricaneWindfieldsUtil.WIND_VELOCITY_60SECS, factor, WINDFIELD_DEFAULT_ELEVATION,
                STANDARD_OPEN_TERRAIN_ROUGHNESS).get(demandType);
        }

        if(!demandUnits.equalsIgnoreCase(UNITS_KT)){
            factor = getCorrectUnitsOfVelocity(factor, UNITS_KT, demandUnits);
        }

        for (int row = 0; row < rows; row++) {
            List<String> rowList = new ArrayList<>();
            for (int col = 0; col < cols; col++) {
                rowList.add(cArr[row][col].multiply(factor).toString());
            }
            retList.add(rowList);
        }

        return retList;
    }

    public static final IncorePoint convertXY2LongLati(IncorePoint center, Complex deltaDist, boolean backwards, String method) {

        double centerLong = center.getLocation().getX();
        double centerLati = center.getLocation().getY();

        double x = deltaDist.getReal();
        double y = deltaDist.getImaginary();

        if (backwards) {
            x = -x;
            y = -y;
        }

        double c = abs(y) / R_EARTH;
        double a = pow(tan(c / 2), 2) / (1 + pow((tan(c / 2)), 2));


        double deltaLati = 2 * asin(sqrt(a)) / Math.PI * 180; //degree
        double lati = 0;
        lati = centerLati + (deltaLati * signum(y));

        double c1 = abs(x) / R_EARTH;
        double a1 = pow(tan(c1 / 2), 2) / (1 + pow((tan(c1 / 2)), 2));

        double deltaLong = 0;
        double longi = 0;

        if (method == "hwind") {
            deltaLong = 2 * asin(sqrt(a1 / pow(cos(PARA / 180 * Math.PI), 2))) / Math.PI * 180;
            longi = centerLong + deltaLong * signum(x);
        } else if (method == "real") { //Migrate these if needed in future calculations.

        } else if (method == "linear1") {

        } else if (method == "linear1") {

        }

        return new IncorePoint(lati + "," + longi);
    }


    /**
     * @param center
     * @param deltaDist
     * @param backwards
     * @return
     */
    public static final IncorePoint convertXY2LongLati(IncorePoint center, Complex deltaDist, boolean backwards) {
        return convertXY2LongLati(center, deltaDist, backwards, "hwind");
    }

    /**
     * @param rho
     * @param theta
     * @return
     */
    public static final Complex convertToPolar(double rho, double theta) {
        return (new Complex(rho * Math.cos(theta), rho * Math.sin(theta)));
    }

    /**
     * @param timeNewRadii
     * @param VTsSimu
     * @param LandfallLoc
     * @param indexLandfall
     * @return
     */
    public static final List<IncorePoint> locateNewTrack(List<Double> timeNewRadii, List<Complex> VTsSimu, IncorePoint LandfallLoc, int indexLandfall) {

        List<IncorePoint> centers = new ArrayList<IncorePoint>(indexLandfall + 1);

        int timesSize = 0;
        timesSize = timeNewRadii.size();

        List<IncorePoint> centersSimu = new ArrayList<IncorePoint>(timesSize);

        centers.add(LandfallLoc);

        for (int i = indexLandfall; i >= 1; i--) {
            double deltaT = 0;
            deltaT = (timeNewRadii.get(i) - timeNewRadii.get(i - 1)) * 3600;
            Complex deltaDist = VTsSimu.get(i - 1).multiply(deltaT);
            centers.add(0, convertXY2LongLati(centers.get(0), deltaDist, true, "hwind"));
        }

        centersSimu.add(centers.get(0));

        for (int i = 1; i < timesSize; i++) {
            double deltaT = 0;
            deltaT = (timeNewRadii.get(i) - timeNewRadii.get(i - 1)) * 3600;
            Complex deltaDist = VTsSimu.get(i - 1).multiply(deltaT);
            centersSimu.add(convertXY2LongLati(centersSimu.get(i - 1), deltaDist, false, "hwind"));
        }
        return centersSimu;
    }

    /**
     * @param center
     * @param resolution
     * @return
     */
    public static final HurricaneGrid defineGrid(IncorePoint center, int resolution, int gridPoints) {

        HurricaneGrid g = new HurricaneGrid();
        double centerLong = center.getLocation().getX();
        double centerLati = center.getLocation().getY();
        double xMax = resolution * gridPoints; //TODO: Use the class const  X_MAX if this is hardcoded value. Talk to PI


        List<Double> xsIntermediate = new ArrayList<Double>();
        List<Double> xs = new ArrayList<Double>();
        List<Double> ys = new ArrayList<Double>();

        int i = 0;
        for (double x = -xMax; x <= xMax; x = x + resolution) {
            xsIntermediate.add(x);
            i++;
        }
        int cords = xsIntermediate.size();

        for (int j = 0; j < cords; j++) {
            for (int k = 0; k < cords; k++) {
                xs.add(xsIntermediate.get(j) * 1000);
                ys.add(xsIntermediate.get(k) * 1000);
            }
        }

        List<Double> latis = new ArrayList<Double>();
        List<Double> longis = new ArrayList<Double>();

        int xyCords = xs.size();

        for (int j = 0; j < xyCords; j++) {
            IncorePoint p = convertXY2LongLati(center, new Complex(xs.get(j), ys.get(j)), false);
            latis.add(p.getLocation().getY());
            longis.add(p.getLocation().getX());
        }

        List<Double> lati = new ArrayList<Double>();
        List<Double> longi = new ArrayList<Double>();

        for (int j = 0; j < cords; j++) {
            lati.add(latis.get(j));
            longi.add(longis.get(j * cords));
        }

        g.setXs(xs);
        g.setYs(ys);
        g.setLongis(longis);
        g.setLatis(latis);
        g.setLati(lati);
        g.setLongi(longi);
        g.setCenter(center);
        return g;
    }

    /**
     * TODO: Does something i dont know. Talk to PI to get a meaningful name
     *
     * @param fui
     * @param fui1
     * @param type
     * @return wi
     */
    public static final Double getWi(Double fui, Double fui1, String type) {

        Double wiNum = pow(fui, -1) * (1 - fui1);
        Double wiDen = pow(fui, -1) * (1 - fui1) + fui1 * (1 - pow(fui, -1));
        Double wi = wiNum / wiDen;

        Double wi1Num = fui1 * (1 - pow(fui, -1));
        Double wi1Den = pow(fui, -1) * (1 - fui1) + fui1 * (1 - pow(fui, -1));
        Double wi1 = wi1Num / wi1Den;

        if (type == "backward") {
            return wi1;
        } else {
            return wi;
        }
    }


    /**
     * Simulates wind speed using Holland model
     *
     * @param r
     * @param theta
     * @param b
     * @param fCorolosis
     * @param rmThetaVsp
     * @param pc
     * @return
     */
    public static final Complex HollandGradientWind(Double r, Double theta, Double b, Double fCorolosis,
                                                    Complex rmThetaVsp, Double pc) {
        Complex expImg = (rmThetaVsp.divide(r)).pow(b).negate().exp();

        Complex sqrtImg = rmThetaVsp.divide(r).pow(b);

        double sqrtRealMulti = (b / RHO) * (PN - pc);
        double sqrtRealAdd = pow(r * fCorolosis / 2, 2);

        Complex vGTemp = expImg.multiply(sqrtImg).multiply(sqrtRealMulti).add(sqrtRealAdd).sqrt().subtract(r * fCorolosis / 2);
        Double vgAbs = vGTemp.abs();

        Complex vG = new Complex(-vgAbs * cos(theta), vgAbs * sin(theta));
        return vG;
    }

    /**
     * @param c
     * @param alpha
     * @return
     */
    public static final Complex rotateVector(Complex c, double alpha) {
        double newAngle = c.getArgument() + alpha;
        Double rho = c.abs();
        Complex rotatedV = new Complex(rho * cos(newAngle), rho * sin(newAngle));
        return rotatedV;
    }


    //finds theta ranges and zone classes to separately model asymmetric wind fields.
    public static final Map<String, Object> rangeWindSpeedComb(JSONArray omegaFitted) {

        int omegaSize = omegaFitted.size();
        List<Integer> omegaMissAll = new ArrayList<Integer>();

        List<List<Integer>> omegaFittedInts = new ArrayList<>();

        //Converting all Longs to Integers
        for (int i = 0; i < omegaSize; i++) {
            List<Long> tempLong = (ArrayList<Long>) omegaFitted.get(i); // init to each row of the array
            List<Integer> temp = tempLong.stream().map(Long::intValue).collect(Collectors.toList());
            omegaFittedInts.add(temp);
        }


        for (int i = 0; i < omegaSize; i++) {
            //List<Long> tempLong = (ArrayList<Long>)omegaFitted.get(i); // init to each row of the array
            //List<Integer> temp = tempLong.stream().map(Long::intValue).collect(Collectors.toList());
            List<Integer> temp = omegaFittedInts.get(i);
            int elemCnt = temp.size();
            List<Integer> temp2 = new ArrayList<Integer>(); //will hold the result of each row

            List<Integer> sep = new ArrayList<Integer>(); //separator
            List<Integer> allSeps = new ArrayList<Integer>();
            for (int j = 0; j < (temp.size() - 1); j++) {
                sep.add(temp.get(j + 1) - temp.get(j));
            }

            int idx = 0;
            for (int e : sep) {
                if (e != 1) {
                    allSeps.add(idx);
                }
                idx++;
            }

            int sepsSize = allSeps.size();

            if (sepsSize == 0) {
                temp2 = Arrays.asList(temp.get(0), temp.get(elemCnt - 1));
            } else {
                for (int k = 0; k < sepsSize; k++) {
                    if (k == 0) {
                        temp2 = ListUtils.union(temp2, Arrays.asList(temp.get(0), temp.get(allSeps.get(k))));    //ncsa.tools.common.utils.ListUtils is also available?!
                    } else {
                        temp2 = ListUtils.union(temp2, Arrays.asList(temp.get(allSeps.get(k - 1) + 1), temp.get(allSeps.get(k))));
                    }
                }

                temp2 = ListUtils.union(temp2, Arrays.asList(temp.get(allSeps.get(sepsSize - 1) + 1), temp.get(elemCnt - 1)));
            }

            omegaMissAll = ListUtils.union(omegaMissAll, temp2);
        }

        List<Integer> divisionS = omegaMissAll.stream().distinct().collect(Collectors.toList());
        divisionS.sort(Comparator.naturalOrder());

        int zones = divisionS.size();
        int[][] thetaRange = new int[zones][2];

        List<List<Integer>> thetaRangesAll = new ArrayList<>();

        for (int i = 0; i < zones; i++) {
            if (i == zones - 1) {
                thetaRange[i][0] = divisionS.get(i);
                thetaRange[i][1] = divisionS.get(0);
            } else {
                thetaRange[i][0] = divisionS.get(i);
                thetaRange[i][1] = divisionS.get(i + 1);
            }

            List<Integer> rangeList = new ArrayList<Integer>();

            if (thetaRange[i][0] <= thetaRange[i][1]) {
                for (int p = thetaRange[i][0]; p <= thetaRange[i][1]; p++) {
                    rangeList.add(p);
                }
            } else {
                //Can I add it in a single loop without changing order?
                for (int p = 1; p <= 16; p++) {
                    if (p >= thetaRange[i][0]) {
                        rangeList.add(p);
                    }
                }
                for (int p = 1; p <= 16; p++) {
                    if (p <= thetaRange[i][1]) {
                        rangeList.add(p);
                    }
                }
            }
            thetaRangesAll.add(rangeList);
        }

        int noClass = omegaSize + 1;

        List<List<Integer>> riS = new ArrayList<>();
        List<Integer> ri = new ArrayList<Integer>();
        List<Integer> riNext = new ArrayList<Integer>();
        for (int i = -1; i < omegaSize; i++) {
            if (i != -1) {
                ri = omegaFittedInts.get(i);
            }

            if (i != omegaSize - 1) {
                riNext = omegaFittedInts.get(i + 1);
            }

            if (i == -1) {
                ri = riNext;
            } else if (i == omegaSize - 1) {
                List<Integer> diff = ListUtils.subtract(OMEGAS_ALL, ri);
                List<Integer> omegaIndexComplement = new ArrayList<Integer>();

                for (int z = diff.get(0) - 1; z <= diff.get(diff.size() - 1) + 1; z++) {
                    omegaIndexComplement.add(z);
                }

                if (omegaIndexComplement.get(0) == 0) {
                    omegaIndexComplement.set(0, 16);
                }

                ri = omegaIndexComplement;
            } else {
                List<Integer> diff = ListUtils.subtract(OMEGAS_ALL, ri);
                List<Integer> omegaIndexComplement = new ArrayList<Integer>();

                for (int z = diff.get(0) - 1; z <= diff.get(diff.size() - 1) + 1; z++) {
                    omegaIndexComplement.add(z);
                }

                //diff.add(0, diff.get(0) -1);
                //diff.add(diff.get(diff.size() -1) +1);

                if (omegaIndexComplement.get(0) == 0) {
                    omegaIndexComplement.set(0, 16);
                }

                ri = ListUtils.intersection(omegaIndexComplement, riNext);
                ri.sort(Comparator.naturalOrder());
            }
            riS.add(ri);

        }

        List<Integer> ri1 = riS.get(0);
        ri1.sort(Comparator.naturalOrder());
        int[] zoneClass = new int[zones];
        List<Integer> fullRange = new ArrayList<Integer>();

        //int p = 0;
        for (int i = 0; i < zones; i++) { //matlab codes uses size(thetaRange,1) which should be the same as zones!?
            List<Integer> tempTheta = thetaRangesAll.get(i);
            tempTheta.sort(Comparator.naturalOrder());


            //if(ListUtils.intersection(tempTheta, ri1).size() > 0){
            if (ri1.containsAll(tempTheta)) {
                zoneClass[i] = 1;
            } else {
                fullRange.add(i + 1);
                //p++;
            }
        }

        for (int e :
            fullRange) {
            List<Integer> tempTheta = thetaRangesAll.get(e - 1);
            for (int i = 0; i < noClass; i++) {
                List<Integer> riCurr = riS.get(i);
                if (riCurr.containsAll(tempTheta)) {
                    zoneClass[e - 1] = i + 1;
                }
            }
        }

        /* Convert theta to angle */

        List<Integer> specRows = new ArrayList<Integer>();

        int specIndex = 0;
        for (int[] thetaR :
            thetaRange) {
            if (thetaR[1] < thetaR[0]) {
                specRows.add(specIndex);
            }
            specIndex++;
        }

        for (int row :
            specRows) {
            if (thetaRange[row][1] == 1) {
                thetaRange[row][1] = 17;
            } else {
                int[] zoneClassLast = new int[zones + 1];
                int[][] thetaRangeLast = new int[zones + 1][2];

                for (int i = 0; i <= row + 2; i++) {
                    if (i <= row - 1) {
                        thetaRangeLast[i] = thetaRange[i];
                    }
                    if (i <= row) {
                        zoneClassLast[i] = zoneClass[i];
                    }

                    if (i == row) {

                        thetaRangeLast[i][0] = thetaRange[i][0];
                        thetaRangeLast[i][1] = 17;
                    }

                    if (i == row + 1) {
                        thetaRangeLast[i][0] = 1;
                        thetaRangeLast[i][1] = thetaRange[i - 1][1];
                        zoneClassLast[i] = zoneClass[i - 1];
                    }

                    if (i == row + 2) {
                        // This code from matlab seems unecessary, the size is initialized as zones+1 and it's trying
                        // to set value at index zones+2. Java will throw index our of bound exception.

                        //thetaRange_(index_spec_row+2:end,:)=thetaRange(index_spec_row+1:end,:);
                        //zone_class_(index_spec_row+2:end)=zone_class(index_spec_row+2:end);
                    }
                }

                thetaRange = thetaRangeLast;
                zoneClass = zoneClassLast;
            }
        }

        double[][] thetaRangeAngle = new double[thetaRange.length][2];

        for (int i = 0; i < thetaRange.length; i++) {
            for (int j = 0; j < thetaRange[i].length; j++) {
                thetaRangeAngle[i][j] = (thetaRange[i][j] - 1) * 22.5 * (Math.PI / 180);
            }
        }

        Map<String, Object> thetaRangeZones = new HashMap<String, Object>();

        thetaRangeZones.put("thetaRange", thetaRangeAngle);
        thetaRangeZones.put("zoneClass", zoneClass);

        return thetaRangeZones;
    }

    public static HurricaneSimulationDataset createHurricaneDataSetFromFile(String filePath, String title, String creator,
                                                                            String description, String datasetType,
                                                                            String simulationAbsTime) throws IOException {
        File file = new File(filePath);
        String datasetId = ServiceUtil.createRasterDataset(file, title, creator, description, datasetType);


        HurricaneSimulationDataset simDataset = new HurricaneSimulationDataset();
        simDataset.setAbsTime(simulationAbsTime);
        simDataset.setDatasetId(datasetId);
        return simDataset;
    }

    public static HurricaneSimulationDataset createHurricaneDataSetFromFiles(List<String> filePaths, String title, String creator,
                                                                             String description, String datasetType,
                                                                             String simulationAbsTime) throws IOException {
        List<File> files = new ArrayList();
        for (String filePath : filePaths) {
            files.add(new File(filePath));
        }
        String datasetId = ServiceUtil.createVisualizationDataset(files, title, creator, description, datasetType);

        HurricaneSimulationDataset simDataset = new HurricaneSimulationDataset();
        simDataset.setAbsTime(simulationAbsTime);
        simDataset.setDatasetId(datasetId);
        return simDataset;
    }


    public static double getCorrectUnitsOfVelocity(double hazardValue, String originalDemandUnits, String requestedDemandUnits) {
        if (originalDemandUnits.equalsIgnoreCase(requestedDemandUnits)){
            return hazardValue;
        }
        else if (originalDemandUnits.equalsIgnoreCase(UNITS_KT) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPS)) {
            return hazardValue * KT2MS;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_KT) && requestedDemandUnits.equalsIgnoreCase(UNITS_KMPH)) {
            return hazardValue * KT2KMPH;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_KT) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPH)) {
            return hazardValue * KT2MPH;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_KT)) {
            return hazardValue * MPH2KT;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_KMPH)) {
            return hazardValue * MPH2KMPH;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPS)) {
            return hazardValue * MPH2MS;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_KMPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_KT)) {
            return hazardValue * KMPH2KT;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_KMPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPH)) {
            return hazardValue * KMPH2MPH;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_KMPH) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPS)) {
            return hazardValue * KMPH2MS;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPS) && requestedDemandUnits.equalsIgnoreCase(UNITS_KT)) {
            return hazardValue * MS2KT;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPS) && requestedDemandUnits.equalsIgnoreCase(UNITS_MPH)) {
            return hazardValue * MS2MPH;
        } else if (originalDemandUnits.equalsIgnoreCase(UNITS_MPS) && requestedDemandUnits.equalsIgnoreCase(UNITS_KMPH)) {
            return hazardValue * MS2KMPH;
        }
        else {
            throw new UnsupportedOperationException("Cannot convert Velocity from " + originalDemandUnits + " to " + requestedDemandUnits);
        }
    }

    /***
     * 1-D data interpolation. Compares to MATLAB interp1()
     * source: http://www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
     * @param x
     * @param y
     * @param xi
     * @return
     * @throws IllegalArgumentException
     */
    public static final double[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

        if (x.length != y.length) {
            throw new IllegalArgumentException("X and Y must be the same length");
        }
        if (x.length == 1) {
            throw new IllegalArgumentException("X must contain more than one value");
        }
        double[] dx = new double[x.length - 1];
        double[] dy = new double[x.length - 1];
        double[] slope = new double[x.length - 1];
        double[] intercept = new double[x.length - 1];

        // Calculate the line equation (i.e. slope and intercept) between each point
        for (int i = 0; i < x.length - 1; i++) {
            dx[i] = x[i + 1] - x[i];
            if (dx[i] == 0) {
                throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
            }
            if (dx[i] < 0) {
                throw new IllegalArgumentException("X must be sorted");
            }
            dy[i] = y[i + 1] - y[i];
            slope[i] = dy[i] / dx[i];
            intercept[i] = y[i] - x[i] * slope[i];
        }

        // Perform the interpolation here
        double[] yi = new double[xi.length];
        for (int i = 0; i < xi.length; i++) {
            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
                yi[i] = Double.NaN;
            } else {
                int loc = Arrays.binarySearch(x, xi[i]);
                if (loc < -1) {
                    loc = -loc - 2;
                    yi[i] = slope[loc] * xi[i] + intercept[loc];
                } else {
                    yi[i] = y[loc];
                }
            }
        }

        return yi;
    }

    /***
     * Converts 60s/3s wind speed at 10 m elevation over the standard open terrain exposure to the corresponding
     * 1-min and 3-seconds sustained wind speed over a user specified exposure at a user interested elevation.
     * @param srcDemand Specifies if input windspeed is 60s or 3s
     * @param windAt10m 1-min sustained wind speed at 10 m elevation for standard open terrain exopsure
     * @param elevation elevation to calculate the wind speed at
     * @param roughness user specified exposure or roughness length, roughness can take values from 0.003 to 2.5 m, values out of the range are not applicable.
     *           For sparsely built-up suburbs roughness~0.2-0.4, for densely built-up suburbs, towns, roughness~0.8-1.2)
     * @return 1-min sustained wind speed and 3-s gust wind speed at specified elevation and roughness
     */

    public static HashMap<String, Double> convertWindfieldVelocity(String srcDemand, Double windAt10m, Double elevation, Double roughness) {
        HashMap<String, Double> convertedWf = new HashMap<>();

        double zos = STANDARD_OPEN_TERRAIN_ROUGHNESS; //surface roughness for the standard open terrain exposure
        double k = VAN_KORMAN_CONSTANT; // Von Karman constant
        double ct60 = 1.29;
        double ct3 = 2.85;
        double[] betaS = {6.5, 6.0, 5.25, 4.85, 4.0};
        double[] zoSs = {0.005, 0.07, 0.3, 1.0, 2.5};

        double beta = interpLinear(zoSs, betaS, new double[]{zos})[0];
        double factorA = 1;
        //convert 1-min sustained wind velocity to hourly mean wind velocity for open terrain and 10 m elevation
        if(srcDemand.equalsIgnoreCase(WIND_VELOCITY_60SECS)) {
            factorA = 1 + pow(beta, 0.5) * ct60 / 2.5 / log(elevation / zos);
        } else if(srcDemand.equalsIgnoreCase(WIND_VELOCITY_3SECS)){
            factorA = 1 + pow(beta, 0.5) * ct3 / 2.5 / log(elevation / zos);
        } else{
            log.error("Error in convertWindfieldVelocity. Provided input demand "+ srcDemand + " is not valid");
        }

        double wfAt3600s10m = windAt10m / factorA;

        //convert hourly mean wind velocity from standard exposure to user specified exposure
        Double wfStarS = wfAt3600s10m * k / log(10 / zos); // friction velocity for the standard exposure
        Double wfStar = wfStarS / pow(zos / roughness, 0.0706); // friction velocity for the user specified exposure
        Double wf3600 = wfStar / k * log(elevation / roughness); //adjusted mean wind speed at height elevation for the user specified exposure

        //covert hourly mean wind velocity for user specifed exposure to 1-min sustained wind velocity
        beta = (roughness < zoSs[0]) ? 6.5 : interpLinear(zoSs, betaS, new double[]{roughness})[0];
        double factor60 = 1 + pow(beta, 0.5) * ct60 / 2.5 / log(elevation / roughness);
        double wfAt60s = factor60 * wf3600;

        //covert hourly mean wind velocity for user specifed exposure to 3-sec gust wind velocity
        double factor3 = 1 + pow(beta, 0.5) * ct3 / 2.5 / log(elevation / roughness);
        double wfAt3s = factor3 * wf3600;

        convertedWf.put(WIND_VELOCITY_3SECS, wfAt3s);
        convertedWf.put(WIND_VELOCITY_60SECS, wfAt60s);

        return convertedWf;
    }

}
