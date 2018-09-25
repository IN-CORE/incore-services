/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Gowtham Naraharisetty (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils;


import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneGrid;
import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import edu.illinois.ncsa.incore.service.hazard.geotools.GeotoolsUtils;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;


import static java.lang.Math.*;


/**
 * Misc utility functions for doing conversion of hazard types and units
 */
public class HurricaneCalc {
    private static final Logger logger = Logger.getLogger(HurricaneCalc.class);




    public static final Complex[][] simulateWindfieldWithCores(JSONObject para, HurricaneGrid grid, Complex vTs,
                                                          JSONArray omegaFitted, JSONArray zonesFitted, JSONArray radiusM){

        ArrayList<Double> thetaRadians = new ArrayList<Double>();

        for(double i=0; i <=360; i = i+ 0.1){
            thetaRadians.add(i*Math.PI/180);
        }

        double bInner = (double)para.get("b_inner");
        double bOuter = (double)para.get("b_outer");
        double fCorolosis = (double)para.get("f");

        JSONArray arrRmThetaVspInner = (JSONArray) para.get("rm_theta_vsp_inner");
        JSONArray arrRmThetaVspOuter = (JSONArray) para.get("rm_theta_vsp_outer");

        List<List<Complex>>rmThetaVspOuter = HurricaneUtil.convert2DComplexArrayToList(arrRmThetaVspOuter);
        List<List<Complex>>rmThetaVspInner = HurricaneUtil.convert2DComplexArrayToList(arrRmThetaVspInner);

        double pc = (double)para.get("pc"); //central Pressure
        JSONArray vgInner = (JSONArray) para.get("vg_inner");
        JSONArray vgOuter = (JSONArray) para.get("vg_outer");

        double rm = (double)para.get("rm_outer"); // rm_inner is not being used at all. why?

        List<List<Integer>> zonesFittedInts = new ArrayList<>();

        //Converting all Longs to Integers
        for(int i=0; i<zonesFitted.size(); i++){
            List<Long> tempLong = (ArrayList<Long>)zonesFitted.get(i); // init to each row of the array
            List<Integer> temp = tempLong.stream().map(Long::intValue).collect(Collectors.toList());
            zonesFittedInts.add(temp);
        }

//        List<List<Integer>> zonesFittedIntsd = zonesFittedInts;
//        int s = zonesFittedInts.size();

        /*
        Convert Cartesian Coordinates to Polar Coordinates
         */
        List<Double> xs = grid.getXs();
        List<Double> ys = grid.getYs();

        int cordSize = xs.size();

        List<Double> thetas = new ArrayList<Double>();
        List<Double> r = new ArrayList<Double>();

        for(int i=0; i< cordSize; i++){
            double th = atan2(xs.get(i), ys.get(i));
            double rx = hypot(xs.get(i), ys.get(i));

            if(th < 0 && th >= -Math.PI ){
                th = th + 2* Math.PI;
            }

            thetas.add(th);
            r.add(rx);
        }

        Map<String,Object> thetaRangeZones = HurricaneUtil.rangeWindSpeedComb(omegaFitted);

        double[][] thetaRange =  (double[][])thetaRangeZones.get("thetaRange");
        int[] zoneClass = (int[]) thetaRangeZones.get("zoneClass");

        int rangeCnt = thetaRange.length;

        Complex[] vGsTotal = new Complex[cordSize]; //cordSize same as thetaSize/rSize

        for(int i = 0; i < rangeCnt; i++){

            int zoneI = zoneClass[i];

            List<List<Complex>> rmThetaVspOuterRi = new ArrayList<List<Complex>>();
            List<List<Complex>> rmThetaVspInnerRi = new ArrayList<List<Complex>>();
            List<List<Double>> vgInnerRi = new ArrayList<>();
            List<List<Double>> vgOuterRi = new ArrayList<>();
            List<Double> vspGInner = new ArrayList<>();
            List<Double> vspGOuter = new ArrayList<>();
            List<List<Complex>> vGsInner = new ArrayList<List<Complex>>();
            List<List<Complex>> vGsOuter = new ArrayList<List<Complex>>();

            int noOfLoops = zonesFittedInts.get(zoneI -1).get(1);

//            for (List<Integer> zone:
//                 zonesFittedInts) {
            for(int j = 0; j < noOfLoops; j++) {
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
//            }

            int nspOuter = rmThetaVspOuterRi.size(); //this should be same a nspInner
            int nspInner = nspOuter; //Not needed. Only added for clarity

            List<Double> rRiList = new ArrayList<Double>();
            List<Double> thetaRiList = new ArrayList<Double>();

            int thetaIndex = 0;
            List<Integer> thetaIndices = new ArrayList<>(); //This is same as RiIndices from matlab
            List<Integer> thetaMinIndices = new ArrayList<>();
            for (double theta:
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

            List<Complex> rowRmThetaVspOuterRi= rmThetaVspOuterRi.get(nspOuter-1);

            // Combine inner and outer wind fields

            int index = 0;
            List<Integer> grIndices = new ArrayList<>();
            List<Integer> lsIndices = new ArrayList<>();

            List<List<Complex>> tempVgsOuter = new ArrayList<>();
            List<List<Complex>> tempVgsInner = new ArrayList<>();
            List<List<Complex>> tempVgsOuterForInner = new ArrayList<>();

            List<List<Double>> tempAbsVgsOuter = new ArrayList<>();
            List<List<Double>> tempAbsVgsInner = new ArrayList<>();


            for (Double rElem:
                rRiList) {
                Complex v = rowRmThetaVspOuterRi.get(thetaMinIndices.get(index));
                Double b = v.abs();
                List<Complex> tempI;
                if( rElem >= b){
                    grIndices.add(index);

                    tempI = vGsOuter.get(index);
                    tempVgsOuter.add(tempI);

                    List<Double> tempIAbs = tempI.stream().map( compl -> compl.abs() ).collect( Collectors.toList() );
                    tempAbsVgsOuter.add(tempIAbs);
                }
                else{
                    lsIndices.add(index);

                    tempI = vGsInner.get(index);
                    tempVgsInner.add(tempI);

                    List<Double> tempIAbs = tempI.stream().map( compl -> compl.abs() ).collect( Collectors.toList() );
                    tempAbsVgsInner.add(tempIAbs);

                    tempVgsOuterForInner.add(vGsOuter.get(index));
                }
                index++;
            }

            int absSizeOuter = tempAbsVgsOuter.size();
            int absSizeInner = tempAbsVgsInner.size();

            //List<List<Integer>> condsO = new ArrayList<>();
            //boolean[][] condsO = new boolean[absSizeOuter][nspOuter+1]; //TODO: condsO can be removed?
            Complex[] combVgsOuter = new Complex[absSizeOuter];
            Complex[] combVgsInner = new Complex[absSizeInner];
            Complex[] riVgs = new Complex[thetaMinIndices.size()];

            for(int j=0; j<= nspOuter; j++) {
                for (int k = 0; k < absSizeOuter; k++) {
                    if(j == 0) {
                        if (tempAbsVgsOuter.get(k).get(j) < vspGOuter.get(j)) {
                            combVgsOuter[k] = tempVgsOuter.get(k).get(j);
                        }
                    }
                    else if(j >=1 && j < nspOuter){
                        if ((tempAbsVgsOuter.get(k).get(j-1) >= vspGOuter.get(j-1)) &&
                            (tempAbsVgsOuter.get(k).get(j) < vspGOuter.get(j))) {
                            Double fui = (tempAbsVgsOuter.get(k).get(j - 1)) / vspGOuter.get(j - 1);
                            Double fui1 = (tempAbsVgsOuter.get(k).get(j)) / vspGOuter.get(j);

                            Double wi = HurricaneUtil.getWi(fui, fui1, "forward");
                            Double wi1 = HurricaneUtil. getWi(fui, fui1, "backward");

                            combVgsOuter[k] = tempVgsOuter.get(k).get(j - 1).multiply(wi).
                                add(tempVgsOuter.get(k).get(j).multiply(wi1));
                        }
                    }
                    else{  // j == nspOuter condition
                        if (tempAbsVgsOuter.get(k).get(j-1) >= vspGOuter.get(j-1)) {
                            combVgsOuter[k] = tempVgsOuter.get(k).get(j - 1);
                        }
                    }
                }
            }

            //TODO: Try to put these loops into a function
            // There is enough variation (inner needs outers params too) that it might be better to keep them separate

            for(int j=0; j<= nspInner; j++) {
                for (int k = 0; k < absSizeInner; k++) {
                    if(j == 0) {
                        if (tempAbsVgsInner.get(k).get(j) < vspGInner.get(j)) {
                            combVgsInner[k] = tempVgsInner.get(k).get(j);
                        }
                    }
                    else if(j >=1 && j < nspOuter){
                        if ((tempAbsVgsInner.get(k).get(j-1) >= vspGInner.get(j-1)) &&
                            (tempAbsVgsInner.get(k).get(j) < vspGInner.get(j))) {
                            Double fui = (tempAbsVgsInner.get(k).get(j - 1)) / vspGInner.get(j - 1);
                            Double fui1 = (tempAbsVgsInner.get(k).get(j)) / vspGInner.get(j);

                            Double wi = HurricaneUtil.getWi(fui, fui1, "forward");
                            Double wi1 = HurricaneUtil.getWi(fui, fui1, "backward");

                            combVgsInner[k] = tempVgsInner.get(k).get(j - 1).multiply(wi).
                                add(tempVgsInner.get(k).get(j).multiply(wi1));
                        }
                    }
                    else{  // j == nspInner condition
                        //TODO: Matlab has wi calculations here that seems to do nothing. Test later if thats true.
                        if (tempAbsVgsInner.get(k).get(j-1) >= vspGInner.get(j-1)) {
                            combVgsInner[k] = tempVgsOuterForInner.get(k).get(j - 1);
                            //TODO: Talk to PI why this is done. For me, it seems like this should be Inner
                        }
                    }
                }
            }

            int tempIndex = 0;
            for (int gr:
                grIndices) {
                riVgs[gr] =combVgsOuter[tempIndex];
                tempIndex++;
            }

            tempIndex = 0;
            for (int ls:
                lsIndices) {
                riVgs[ls] =combVgsInner[tempIndex];
                tempIndex++;
            }

            int seqCounter = 0;
            for (int thIndex:
                thetaIndices) {
                vGsTotal[thIndex] = riVgs[seqCounter];
                seqCounter++;
            }

            int innerBlah = 1;
        }

        Complex[] vsRotated = convertToSurfaceWind(vGsTotal, vTs, rm, r);
        Complex[][] vsReduced = applyReductionFactor(vsRotated, grid.getLati(), grid.getLongi(), radiusM);



        return vsReduced;
    }


    /**
     *
     * @param vGsTotal
     * @param vTs
     * @param rmOuter
     * @param radians
     * @return
     */
    public  static final Complex[] convertToSurfaceWind(Complex[] vGsTotal, Complex vTs, double rmOuter, List<Double> radians) {

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

//        Complex[][] vsFinal = new Complex[pointSize][pointSize];
//
//        int cord = 0;
//        for (int col = 0; col < pointSize; col++) {
//            for (int row = 0; row < pointSize; row++) {
//                vsFinal[row][col] = vSurfRot[cord];
//                cord++;
//            }
//        }
//
//        return vsFinal;
    }


    public  static final Complex[][] applyReductionFactor(Complex[] vs, List<Double> latis, List<Double> longis, JSONArray radiusM){


        try {
            //TODO: This will change after reduction code works
            // file path for land polygon
            //String dslvPolygon = "/Users/vnarah2/IdeaProjects/incorev2/server/hazard-service/src/main/data/hurricane/tm_north_america_dislvd.shp";
            String dslvPolygon = "tm_north_america_dislvd.shp";
            // file path for country boundary polygon
            //String sprPolygon = "/Users/vnarah2/IdeaProjects/incorev2/server/hazard-service/src/main/data/hurricane/tm_north_america_country.shp";
            String sprPolygon = "tm_north_america_country.shp";

            SimpleFeatureCollection dslvFeatures = GeotoolsUtils.GetSimpleFeatureCollectionFromPath(dslvPolygon);
            SimpleFeatureCollection sprFeatures = GeotoolsUtils.GetSimpleFeatureCollectionFromPath(sprPolygon);


            int cord = 0;
            int pointSize = (int) sqrt(vs.length);

            Complex[][] vsReduced = new Complex[pointSize][pointSize];
            boolean performReduction = true; // only for testing

            SpatialIndexFeatureCollection featureIndex;
            DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
            featureIndex = new SpatialIndexFeatureCollection(dslvFeatures.getSchema());
            featureIndex.addAll(dslvFeatures);
            GeodeticCalculator gc = new GeodeticCalculator(crs);

            final double searchDistLimit = featureIndex.getBounds().getSpan(0);
            // give enough distance for the minimum distance to start
            double minDist = searchDistLimit + 1.0e-6;

            for (int col = 0; col < pointSize; col++) {
                double lon = longis.get(col);
                for (int row = 0; row < pointSize; row++) {
                    double lat = latis.get(row);
                    double reductionFactor = 1;
                    if(performReduction) {
                        boolean isContained = true; //removed function as it is taking .3 secs for each call
                        //boolean isContained = GeotoolsUtils.isPointInPolygon(dslvFeatures, lat, lon);
                        //System.out.println(isContained+"--"+lat+","+lon);
                        int zone = 0;

                        JSONArray ar = new JSONArray();

                        if (isContained) {
                            // if it is on the land, get the country name
                            String name = GeotoolsUtils.getUnderlyingFieldValueFromPoint(sprFeatures, "NAME", lat, lon);
                            //System.out.println(":::"+name);

                            if (name.equals("united states")) {
                                ar = (JSONArray) ((JSONObject) radiusM.get(1)).get("usa");
                            } else if (name.equals("mexico")) {
                                ar = (JSONArray) ((JSONObject) radiusM.get(0)).get("mexico");
                            } else if (name.equals("cuba")) {
                                ar = (JSONArray) ((JSONObject) radiusM.get(2)).get("cuba");
                            } else if (name.equals("jamaica")) {
                                ar = (JSONArray) ((JSONObject) radiusM.get(3)).get("jam");
                            }

                            // get shortest km distance to coastal line
                            //double shortestDist = GeotoolsUtils.FindShortestDistanceFromPointToFeatures(dslvFeatures, lat, lon);
                            if (ar.size() > 0) {
                                double shortestDist = GeotoolsUtils.CalcShortestDistanceFromPointToFeatures(featureIndex,
                                    lat, lon, gc, crs, searchDistLimit, minDist);

                                if (shortestDist <= 10) {
                                    zone = 0;
                                } else if (shortestDist > 10 && shortestDist <= 50) {
                                    zone = 1;
                                } else if (shortestDist > 50 && shortestDist <= 100) {
                                    zone = 2;
                                } else if (shortestDist > 100 && shortestDist <= 300) {
                                    zone = 3;
                                } else {
                                    zone = 4;
                                }
                                reductionFactor = (Double) ar.get(zone);
                            }

                        } else {
                            //Throw 404? and say not point not in north america? or return 1?
                        }
                    }
                    vsReduced[row][col] = vs[cord].multiply(reductionFactor);
                    cord++;
                }
            }
            return vsReduced;
        }catch (Exception ex) {
            throw new NotFoundException("Shapefile Not found");
        }


    }







}
