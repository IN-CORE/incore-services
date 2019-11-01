/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Gowtham Naraharisetty (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.service.hazard.dao.DBHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HistoricHurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneGrid;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulation;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulationEnsemble;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;


/**
 * Misc utility functions for doing conversion of hazard types and units
 */
public class HurricaneCalc {
    private static final Logger logger = Logger.getLogger(HurricaneCalc.class);


    public static HurricaneSimulationEnsemble simulateHurricane(String username, String Authorization, double transD, IncorePoint landfallLoc, String model,
                                                                String demandType, String demandUnits, int resolution, int gridPoints, String rfMethod) {
        //TODO: Comeup with a better name for gridPoints
        //TODO: Can resolution be double? It's being hardcoded in Grid calculation
        //This function simulates wind fields for the selected data-driven model

        DBHurricaneRepository historicHurrRepo = new DBHurricaneRepository();
        HistoricHurricane historicHurricane = historicHurrRepo.getHurricaneByModel(model);
        JSONObject params = historicHurricane.getHurricaneParameters();

        // May be it's better to use List
        List<String> Vt0New = (List<String>) params.get("VTs_o_new");
        List<String> times = (List<String>) params.get("times");
        long longLandfall = (long) params.get("index_landfall");

        int indexLandfall = toIntExact(longLandfall) - 1; //Converting mat index to java
        //IncorePoint landfallPoint = landfallLoc.getLocation();
        List<Double> timeNewRadii = (List<Double>) params.get("time_radii_new");

        Complex vt0 = HurricaneUtil.parseComplexString(Vt0New.get(0), 'j');


        double x1 = vt0.getReal();
        double y1 = vt0.getImaginary();

        double ab = vt0.abs();
        double si = sin(toRadians(transD));
        double co = cos(toRadians(transD));

        double x2 = ab * si;
        double y2 = ab * co;

        double th = atan2(x1 * y2 - y1 * x2, x1 * x2 + y1 * y2);

        List<Complex> VTsSimu = new ArrayList<Complex>();

        for (String s : Vt0New) {
            Complex c1 = HurricaneUtil.parseComplexString(s, 'j');
            double abNew = c1.abs();
            double angle = c1.getArgument();

            VTsSimu.add(HurricaneUtil.convertToPolar(abNew, angle + th));
        }

        List<IncorePoint> track = HurricaneUtil.locateNewTrack(timeNewRadii, VTsSimu, landfallLoc, indexLandfall);

        if (!model.toLowerCase().equals("isabel") && !model.toLowerCase().equals("frances")) {
            VTsSimu.remove(indexLandfall);
            track.remove(indexLandfall);
        }

        JSONArray para = (JSONArray) params.get("para");
        JSONArray omegaFitted = (JSONArray) params.get("omega_miss_fitted");
        JSONArray radiusM = (JSONArray) params.get("Rs");
        JSONArray zonesFitted = (JSONArray) params.get("contouraxis_zones_fitted");

        int paramCt = para.size();

        List<Complex[][]> VsTotal = new ArrayList<>();
        List<List<Double>> gridLatis = new ArrayList<>();
        List<List<Double>> gridLongs = new ArrayList<>();
        List<String> absTime = new ArrayList<>();

        List<HurricaneSimulation> hSimulations = new ArrayList<>();
        List<String> centers = new ArrayList<>();
        List<String> centerVel = new ArrayList<>();

        try {
//            for(int i=0; i<paramCt; i++){
//                HurricaneSimulation hsim = HurricaneCalc.setSimulationWithWindfield((JSONObject) para.get(i), times.get(i),
//                    track.get(i), resolution, gridPoints, VTsSimu.get(i), (JSONArray) omegaFitted.get(i),
//                    (JSONArray) zonesFitted.get(i), (JSONArray)radiusM.get(i), rfMethod);
//                hSimulations.add(hsim);
//            }

            //int logicalThreads = 7;
            int logicalThreads = 1;
            logicalThreads = Runtime.getRuntime().availableProcessors(); //TODO Logic can be improved?
            if (logicalThreads > 3) {
                logicalThreads--;
            }
            ForkJoinPool forkJoinPool = new ForkJoinPool(logicalThreads);

            List<Integer> pList = IntStream.rangeClosed(0, paramCt - 1).boxed().collect(Collectors.toList());

            final Callable<List<HurricaneSimulation>> hurrSims = () -> {
                pList.parallelStream().forEach(i -> {
                    hSimulations.add(HurricaneCalc.setSimulationWithWindfield((JSONObject) para.get(i), times.get(i),
                        track.get(i), demandType, demandUnits, resolution, gridPoints, VTsSimu.get(i), (JSONArray) omegaFitted.get(i),
                        (JSONArray) zonesFitted.get(i), (JSONArray) radiusM.get(i), rfMethod));

                });
                return hSimulations;
            };

            List<HurricaneSimulation> parallelSimResults = forkJoinPool.submit(hurrSims).get();
            parallelSimResults.sort(HurricaneSimulation::compareTo);
            HurricaneSimulationEnsemble hEnsemble = new HurricaneSimulationEnsemble();
            hEnsemble.setResolution(resolution);
            hEnsemble.setTransD(transD);
            hEnsemble.setLandfallLocation(landfallLoc.toString());
            hEnsemble.setModelUsed(model);
            hEnsemble.setTimes(times);
            hEnsemble.setHurricaneSimulations(parallelSimResults);
            return hEnsemble;
        } catch (Exception e) {
            throw new NotFoundException("Failed Creating the Hurricane");
        }
        //TODO: Add finally to cleanup all threads. Is it even needed?
    }

    public static final HurricaneSimulation setSimulationWithWindfield(JSONObject para, String time, IncorePoint center,
                                                                       String demandType, String demandUnits, int resolution, int gridPoints,
                                                                       Complex VTsSimu, JSONArray omegaFitted, JSONArray zonesFitted,
                                                                       JSONArray radiusM, String rfMethod) {
        HurricaneSimulation hsim = new HurricaneSimulation();
        hsim.setAbsTime(time);


        HurricaneGrid hgrid = HurricaneUtil.defineGrid(center, resolution, gridPoints);

        hsim.setGridLats(hgrid.getLati());
        hsim.setGridLongs(hgrid.getLongi());

        Complex[][] vsFinal = HurricaneCalc.simulateWindfieldWithCores(para, hgrid, VTsSimu, omegaFitted, zonesFitted,
            radiusM, rfMethod);

        List<List<String>> strList = HurricaneUtil.convert2DComplexArrayToStringList(vsFinal, demandType, demandUnits);
        //hsim.setSurfaceVelocity(strList); //Uncomment to see complex values too
        hsim.setSurfaceVelocityAbs(HurricaneUtil.convert2DComplexArrayToAbsList(vsFinal, demandType, demandUnits));


        IncorePoint ctr = hgrid.getCenter();
        hsim.setGridCenter(ctr.toString());
        int lonPos = hgrid.getLongi().indexOf(ctr.getLocation().getX()); //col
        int latPos = hgrid.getLati().indexOf(ctr.getLocation().getY()); //row

        //Use this is we want to display the velocity at the center point.
        double centerVelocityAbs = hsim.getSurfaceVelocityAbs().get(latPos - 1).get(lonPos);
        String centerVelocity = strList.get(latPos - 1).get(lonPos);
        hsim.setCenterVelocity(centerVelocity);
        hsim.setCenterVelAbs(centerVelocityAbs);

        return hsim;
    }


    public static final Complex[][] simulateWindfieldWithCores(JSONObject para, HurricaneGrid grid, Complex vTs,
                                                               JSONArray omegaFitted, JSONArray zonesFitted, JSONArray radiusM, String rfMethod) {

        ArrayList<Double> thetaRadians = new ArrayList<Double>();

        for (double i = 0; i <= 360; i = i + 0.1) {
            thetaRadians.add(i * Math.PI / 180);
        }

        double bInner = (double) para.get("b_inner");
        double bOuter = (double) para.get("b_outer");
        double fCorolosis = (double) para.get("f");

        JSONArray arrRmThetaVspInner = (JSONArray) para.get("rm_theta_vsp_inner");
        JSONArray arrRmThetaVspOuter = (JSONArray) para.get("rm_theta_vsp_outer");

        List<List<Complex>> rmThetaVspOuter = HurricaneUtil.convert2DComplexArrayToList(arrRmThetaVspOuter);
        List<List<Complex>> rmThetaVspInner = HurricaneUtil.convert2DComplexArrayToList(arrRmThetaVspInner);

        double pc = (double) para.get("pc"); //central Pressure
        JSONArray vgInner = (JSONArray) para.get("vg_inner");
        JSONArray vgOuter = (JSONArray) para.get("vg_outer");

        double rm = (double) para.get("rm_outer"); // rm_inner is not being used at all. why?

        List<List<Integer>> zonesFittedInts = new ArrayList<>();

        //Converting all Longs to Integers
        for (int i = 0; i < zonesFitted.size(); i++) {
            List<Long> tempLong = (ArrayList<Long>) zonesFitted.get(i); // init to each row of the array
            List<Integer> temp = tempLong.stream().map(Long::intValue).collect(Collectors.toList());
            zonesFittedInts.add(temp);
        }


        /*
        Convert Cartesian Coordinates to Polar Coordinates
         */
        List<Double> xs = grid.getXs();
        List<Double> ys = grid.getYs();

        int cordSize = xs.size();

        List<Double> thetas = new ArrayList<Double>();
        List<Double> r = new ArrayList<Double>();

        for (int i = 0; i < cordSize; i++) {
            double th = atan2(xs.get(i), ys.get(i));
            double rx = hypot(xs.get(i), ys.get(i));

            if (th < 0 && th >= -Math.PI) {
                th = th + 2 * Math.PI;
            }

            thetas.add(th);
            r.add(rx);
        }

        Map<String, Object> thetaRangeZones = HurricaneUtil.rangeWindSpeedComb(omegaFitted);

        double[][] thetaRange = (double[][]) thetaRangeZones.get("thetaRange");
        int[] zoneClass = (int[]) thetaRangeZones.get("zoneClass");

        int rangeCnt = thetaRange.length;

        Complex[] vGsTotal = new Complex[cordSize]; //cordSize same as thetaSize/rSize

        for (int i = 0; i < rangeCnt; i++) {

            int zoneI = zoneClass[i];

            List<List<Complex>> rmThetaVspOuterRi = new ArrayList<List<Complex>>();
            List<List<Complex>> rmThetaVspInnerRi = new ArrayList<List<Complex>>();
            List<List<Double>> vgInnerRi = new ArrayList<>();
            List<List<Double>> vgOuterRi = new ArrayList<>();
            List<Double> vspGInner = new ArrayList<>();
            List<Double> vspGOuter = new ArrayList<>();
            List<List<Complex>> vGsInner = new ArrayList<List<Complex>>();
            List<List<Complex>> vGsOuter = new ArrayList<List<Complex>>();

            int noOfLoops = zonesFittedInts.get(zoneI - 1).get(1);

            for (int j = 0; j < noOfLoops; j++) {
                rmThetaVspInnerRi.add((List<Complex>) rmThetaVspInner.get(j)); // Casting to List<Complex> is only casting as list of strings
                rmThetaVspOuterRi.add((List<Complex>) rmThetaVspOuter.get(j));
                List<Double> vgI = (List<Double>) vgInner.get(j);
                List<Double> vgO = (List<Double>) vgOuter.get(j);

                double meanInner = vgI.stream().mapToDouble(d -> d).average().orElse(0.0);
                double meanOuter = vgO.stream().mapToDouble(d -> d).average().orElse(0.0);

                vspGInner.add(meanInner);
                vspGOuter.add(meanOuter);

                vgInnerRi.add(vgI);
                vgOuterRi.add(vgO);
            }


            int nspOuter = rmThetaVspOuterRi.size(); //this should be same a nspInner
            int nspInner = nspOuter; //Not needed. Only added for clarity

            List<Double> rRiList = new ArrayList<Double>();
            List<Double> thetaRiList = new ArrayList<Double>();

            int thetaIndex = 0;
            List<Integer> thetaIndices = new ArrayList<>(); //This is same as RiIndices from matlab
            List<Integer> thetaMinIndices = new ArrayList<>();
            for (double theta :
                thetas) {
                if (theta >= thetaRange[i][0] && theta < thetaRange[i][1]) {

                    thetaIndices.add(thetaIndex);

                    double rRi = r.get(thetaIndex);
                    double thetaRi = theta;
                    rRiList.add(rRi);
                    thetaRiList.add(thetaRi);
                    List<Double> thetaDiff = new ArrayList<>();
                    for (int z = 0; z < thetaRadians.size(); z++) {
                        thetaDiff.add(abs(thetaRi - thetaRadians.get(z)));
                    }

                    int minIndex = thetaDiff.indexOf(Collections.min(thetaDiff)); // There will always be a single min value in radians List

                    thetaMinIndices.add(minIndex);
                    List<Complex> vGsInnerRi = new ArrayList<>();
                    List<Complex> vGsOuterRi = new ArrayList<>();

                    for (int nsp = 0; nsp < nspOuter; nsp++) {
                        vGsOuterRi.add(HurricaneUtil.HollandGradientWind(rRi, thetaRi, bOuter, fCorolosis,
                            rmThetaVspOuterRi.get(nsp).get(minIndex), pc));
                        vGsInnerRi.add(HurricaneUtil.HollandGradientWind(rRi, thetaRi, bInner, fCorolosis,
                            rmThetaVspInnerRi.get(nsp).get(minIndex), pc));
                    }
                    vGsOuter.add(vGsOuterRi);
                    vGsInner.add(vGsInnerRi);
                }
                thetaIndex++;
            }

            List<Complex> rowRmThetaVspOuterRi = rmThetaVspOuterRi.get(nspOuter - 1);

            // Combine inner and outer wind fields

            int index = 0;
            List<Integer> grIndices = new ArrayList<>();
            List<Integer> lsIndices = new ArrayList<>();

            List<List<Complex>> tempVgsOuter = new ArrayList<>();
            List<List<Complex>> tempVgsInner = new ArrayList<>();
            List<List<Complex>> tempVgsOuterForInner = new ArrayList<>();

            List<List<Double>> tempAbsVgsOuter = new ArrayList<>();
            List<List<Double>> tempAbsVgsInner = new ArrayList<>();


            for (Double rElem :
                rRiList) {
                Complex v = rowRmThetaVspOuterRi.get(thetaMinIndices.get(index));
                Double b = v.abs();
                List<Complex> tempI;
                if (rElem >= b) {
                    grIndices.add(index);

                    tempI = vGsOuter.get(index);
                    tempVgsOuter.add(tempI);

                    List<Double> tempIAbs = tempI.stream().map(compl -> compl.abs()).collect(Collectors.toList());
                    tempAbsVgsOuter.add(tempIAbs);
                } else {
                    lsIndices.add(index);

                    tempI = vGsInner.get(index);
                    tempVgsInner.add(tempI);

                    List<Double> tempIAbs = tempI.stream().map(compl -> compl.abs()).collect(Collectors.toList());
                    tempAbsVgsInner.add(tempIAbs);

                    tempVgsOuterForInner.add(vGsOuter.get(index));
                }
                index++;
            }

            int absSizeOuter = tempAbsVgsOuter.size();
            int absSizeInner = tempAbsVgsInner.size();

            Complex[] combVgsOuter = new Complex[absSizeOuter];
            Complex[] combVgsInner = new Complex[absSizeInner];
            Complex[] riVgs = new Complex[thetaMinIndices.size()];

            for (int j = 0; j <= nspOuter; j++) {
                for (int k = 0; k < absSizeOuter; k++) {
                    if (j == 0) {
                        if (tempAbsVgsOuter.get(k).get(j) < vspGOuter.get(j)) {
                            combVgsOuter[k] = tempVgsOuter.get(k).get(j);
                        }
                    } else if (j >= 1 && j < nspOuter) {
                        if ((tempAbsVgsOuter.get(k).get(j - 1) >= vspGOuter.get(j - 1)) &&
                            (tempAbsVgsOuter.get(k).get(j) < vspGOuter.get(j))) {
                            Double fui = (tempAbsVgsOuter.get(k).get(j - 1)) / vspGOuter.get(j - 1);
                            Double fui1 = (tempAbsVgsOuter.get(k).get(j)) / vspGOuter.get(j);

                            Double wi = HurricaneUtil.getWi(fui, fui1, "forward");
                            Double wi1 = HurricaneUtil.getWi(fui, fui1, "backward");

                            combVgsOuter[k] = tempVgsOuter.get(k).get(j - 1).multiply(wi).
                                add(tempVgsOuter.get(k).get(j).multiply(wi1));
                        }
                    } else {  // j == nspOuter condition
                        if (tempAbsVgsOuter.get(k).get(j - 1) >= vspGOuter.get(j - 1)) {
                            combVgsOuter[k] = tempVgsOuter.get(k).get(j - 1);
                        }
                    }
                }
            }

            // There is enough variation (inner needs outers params too) that it might be better to keep them separate
            for (int j = 0; j <= nspInner; j++) {
                for (int k = 0; k < absSizeInner; k++) {
                    if (j == 0) {
                        if (tempAbsVgsInner.get(k).get(j) < vspGInner.get(j)) {
                            combVgsInner[k] = tempVgsInner.get(k).get(j);
                        }
                    } else if (j >= 1 && j < nspOuter) {
                        if ((tempAbsVgsInner.get(k).get(j - 1) >= vspGInner.get(j - 1)) &&
                            (tempAbsVgsInner.get(k).get(j) < vspGInner.get(j))) {
                            Double fui = (tempAbsVgsInner.get(k).get(j - 1)) / vspGInner.get(j - 1);
                            Double fui1 = (tempAbsVgsInner.get(k).get(j)) / vspGInner.get(j);

                            Double wi = HurricaneUtil.getWi(fui, fui1, "forward");
                            Double wi1 = HurricaneUtil.getWi(fui, fui1, "backward");

                            combVgsInner[k] = tempVgsInner.get(k).get(j - 1).multiply(wi).
                                add(tempVgsInner.get(k).get(j).multiply(wi1));
                        }
                    } else {  // j == nspInner condition
                        //TODO: Matlab has wi calculations here that seems to do nothing. Test later if thats true.
                        if (tempAbsVgsInner.get(k).get(j - 1) >= vspGInner.get(j - 1)) {
                            combVgsInner[k] = tempVgsOuterForInner.get(k).get(j - 1);
                        }
                    }
                }
            }

            int tempIndex = 0;
            for (int gr :
                grIndices) {
                riVgs[gr] = combVgsOuter[tempIndex];
                tempIndex++;
            }

            tempIndex = 0;
            for (int ls :
                lsIndices) {
                riVgs[ls] = combVgsInner[tempIndex];
                tempIndex++;
            }

            int seqCounter = 0;
            for (int thIndex :
                thetaIndices) {
                vGsTotal[thIndex] = riVgs[seqCounter];
                seqCounter++;
            }

            int innerBlah = 1;
        }

        Complex[] vsRotated = convertToSurfaceWind(vGsTotal, vTs, rm, r);
        Complex[][] vsReduced = applyReductionFactor(vsRotated, grid.getLati(), grid.getLongi(), grid.getCenter(), radiusM, rfMethod);


        return vsReduced;
    }


    /**
     * @param vGsTotal
     * @param vTs
     * @param rmOuter
     * @param radians
     * @return
     */
    public static final Complex[] convertToSurfaceWind(Complex[] vGsTotal, Complex vTs, double rmOuter, List<Double> radians) {

        double[] fdp = new double[radians.size()];

        Complex[] vSurfRot = new Complex[radians.size()];

        int idx = 0;

        double a1 = 15;
        double a3 = 30;
        double a2 = (a3 - a1) / 0.2;

        int cordsSize = radians.size();
        //int pointSize = (int) sqrt(cordsSize); //This is always going to be int

        double[] alpha = new double[cordsSize];

        for (double r :
            radians) {

            fdp[idx] = (rmOuter * r) / (pow(rmOuter, 2) + pow(r, 2));
            if (vGsTotal[idx] != null) {
                Complex vSurf = (vGsTotal[idx].multiply(HurricaneUtil.FR)).add(vTs.multiply(fdp[idx]));
                // Rotate counter-clockwise
                if (r < rmOuter) {
                    alpha[idx] = a1 * (r / rmOuter) * (Math.PI / 180);
                } else if (r >= rmOuter && r < 1.2 * rmOuter) {
                    alpha[idx] = a1 + a2 * (r / (rmOuter - 1)) * (Math.PI / 180);
                } else if (r >= 1.2 * rmOuter) { //this could just be else
                    alpha[idx] = a3 * (Math.PI / 180);
                }

                vSurfRot[idx] = (HurricaneUtil.rotateVector(vSurf, alpha[idx])).divide(HurricaneUtil.KT2MS);
            } else { // Adding this condition to handle 0,0 complex number when it's returning null. !HACK! fix the source.
                vSurfRot[idx] = new Complex(0, 0);
            }
            idx++;
        }

        return vSurfRot;
    }


    public static final Complex[][] applyReductionFactor(Complex[] vs, List<Double> latis, List<Double> longis, IncorePoint center,
                                                         JSONArray radiusM, String rfMethod) {
        try {
            int pointSize = (int) sqrt(vs.length);

            Complex[][] vsReduced = new Complex[pointSize][pointSize];
            boolean performReduction = true; // only for testing

            DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
            GeodeticCalculator gc = new GeodeticCalculator(crs);
            double reductionFactor = 1;

            if (performReduction) {
                if (rfMethod.equals("circular")) {
                    Point cPoint = center.getLocation();
                    double cLat = cPoint.getY();
                    double cLong = cPoint.getX();

                    HashMap<String, List<Double>> allCountryRadii = new HashMap<String, List<Double>>();

                    GeometryFactory gf = new GeometryFactory();

                    Point tangentPointUsa = GeotoolsUtils.CalcTangentPointToFeatures(GISHurricaneUtils.usaPolygon,
                        cLat, cLong, gc, crs, GISHurricaneUtils.searchDistLimit, GISHurricaneUtils.minSearchDist);
                    Point tangentPointMex = GeotoolsUtils.CalcTangentPointToFeatures(GISHurricaneUtils.mexicoPolygon,
                        cLat, cLong, gc, crs, GISHurricaneUtils.searchDistLimit, GISHurricaneUtils.minSearchDist);
                    Point tangentPointCuba = GeotoolsUtils.CalcTangentPointToFeatures(GISHurricaneUtils.cubaPolygon,
                        cLat, cLong, gc, crs, GISHurricaneUtils.searchDistLimit, GISHurricaneUtils.minSearchDist);
                    Point tangentPointJam = GeotoolsUtils.CalcTangentPointToFeatures(GISHurricaneUtils.jamaicaPolygon,
                        cLat, cLong, gc, crs, GISHurricaneUtils.searchDistLimit, GISHurricaneUtils.minSearchDist);


                    double tangentDistUsa = GISHurricaneUtils.getGeogDistance(center.getLocation(), tangentPointUsa, "hwind");
                    double tangentDistMex = GISHurricaneUtils.getGeogDistance(center.getLocation(), tangentPointMex, "hwind");
                    double tangentDistCuba = GISHurricaneUtils.getGeogDistance(center.getLocation(), tangentPointCuba, "hwind");
                    double tangentDistJam = GISHurricaneUtils.getGeogDistance(center.getLocation(), tangentPointJam, "hwind");

                    allCountryRadii.put("usa", GISHurricaneUtils.createRegionRadii(tangentDistUsa));
                    allCountryRadii.put("mexico", GISHurricaneUtils.createRegionRadii(tangentDistMex));
                    allCountryRadii.put("cuba", GISHurricaneUtils.createRegionRadii(tangentDistCuba));
                    allCountryRadii.put("jamaica", GISHurricaneUtils.createRegionRadii(tangentDistJam));

                    int cord = 0;
                    for (int col = 0; col < pointSize; col++) {
                        double lon = longis.get(col);
                        for (int row = 0; row < pointSize; row++) {
                            double lat = latis.get(row);
                            Point currPoint = gf.createPoint(new Coordinate(lon, lat));

                            Double currPointDistance = GISHurricaneUtils.getGeogDistance(currPoint, center.getLocation(), "hwind");

                            String countryName = GISHurricaneUtils.getCountryFromNAPolygons(lat, lon);
                            reductionFactor = 1;

                            if (!countryName.equals("")) {
                                List<Double> regionsRadii = allCountryRadii.get(countryName);
                                JSONArray rfArr = getCountryRfMatrix(countryName, radiusM);

                                if (currPointDistance <= regionsRadii.get(0)) {
                                    reductionFactor = (Double) rfArr.get(0);
                                } else if (currPointDistance > regionsRadii.get(0) && currPointDistance <= regionsRadii.get(1)) {
                                    reductionFactor = (Double) rfArr.get(1);
                                } else if (currPointDistance > regionsRadii.get(1) && currPointDistance <= regionsRadii.get(2)) {
                                    reductionFactor = (Double) rfArr.get(2);
                                } else if (currPointDistance > regionsRadii.get(2) && currPointDistance <= regionsRadii.get(3)) {
                                    reductionFactor = (Double) rfArr.get(3);
                                } else {
                                    reductionFactor = (Double) rfArr.get(4);
                                }
                            }
                            vsReduced[row][col] = vs[cord].multiply(reductionFactor);
                            cord++;
                        }
                    }

                } else {
                    int cord = 0;
                    for (int col = 0; col < pointSize; col++) {
                        double lon = longis.get(col);
                        for (int row = 0; row < pointSize; row++) {
                            double lat = latis.get(row);

                            boolean isContained = true; //removed function as it is taking .3 secs for each call
                            //boolean isContained = GeotoolsUtils.isPointInPolygon(dslvFeatures, lat, lon);
                            int zone = 0;

                            if (isContained) {
                                // if it is on the land, get the country name
                                SimpleFeatureCollection sfc = GISHurricaneUtils.countriesFeatures;
                                String countryName = GeotoolsUtils.getUnderlyingFieldValueFromPoint(sfc, "NAME", lat, lon);
                                JSONArray ar = getCountryRfMatrix(countryName, radiusM);

                                // get shortest km distance to coastal line
                                if (ar.size() > 0) {
                                    double shortestDist = GeotoolsUtils.CalcShortestDistanceFromPointToFeatures(GISHurricaneUtils.continentFeatureIndex,
                                        lat, lon, gc, crs, GISHurricaneUtils.searchDistLimit, GISHurricaneUtils.minSearchDist);
                                    zone = getZone(shortestDist);
                                    reductionFactor = (Double) ar.get(zone);
                                }
                            } else {
                                //Throw 404? and say not point not in north america? or return 1?
                            }

                            vsReduced[row][col] = vs[cord].multiply(reductionFactor);
                            cord++;
                        }
                    }
                }
            } else {
                int cord = 0;
                for (int col = 0; col < pointSize; col++) {
                    for (int row = 0; row < pointSize; row++) {
                        vsReduced[row][col] = vs[cord].multiply(reductionFactor);
                        cord++;
                    }
                }
            }
            return vsReduced;
        } catch (Exception ex) {
            throw new NotFoundException("Shapefile Not found");
        }
    }

    public static int getZone(double shortestDist) {
        if (shortestDist <= 10) {
            return 0;
        } else if (shortestDist > 10 && shortestDist <= 50) {
            return 1;
        } else if (shortestDist > 50 && shortestDist <= 100) {
            return 2;
        } else if (shortestDist > 100 && shortestDist <= 300) {
            return 3;
        } else {
            return 4;
        }
    }

    public static JSONArray getCountryRfMatrix(String name, JSONArray radiusM) {
        JSONArray ar = new JSONArray();
        if (name.equals("united states") || name.equals("usa")) {
            ar = (JSONArray) ((JSONObject) radiusM.get(1)).get("usa");
        } else if (name.equals("mexico")) {
            ar = (JSONArray) ((JSONObject) radiusM.get(0)).get("mexico");
        } else if (name.equals("cuba")) {
            ar = (JSONArray) ((JSONObject) radiusM.get(2)).get("cuba");
        } else if (name.equals("jamaica")) {
            ar = (JSONArray) ((JSONObject) radiusM.get(3)).get("jam");
        }
        return ar;
    }


}
