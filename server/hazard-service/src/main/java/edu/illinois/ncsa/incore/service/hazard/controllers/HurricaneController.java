/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.DBHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulation;

//import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneGrid;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneSimulationEnsemble;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneCalc;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneUtil;
import org.apache.log4j.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject; //TODO: org.json.* vs org.json.simple.*
// http://www.rojotek.com/blog/2009/05/07/a-review-of-5-java-json-libraries/

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.*;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexFormat;

import static java.lang.Math.*;


@Path("hurricanes")
public class HurricaneController {
    private static final Logger logger = Logger.getLogger(HurricaneController.class);

    @Inject
    private IAuthorizer authorizer;


    private GeometryFactory factory = new GeometryFactory();


    private DBHurricaneRepository hurricaneRepo = new DBHurricaneRepository();


    @GET
    @Path("{coast}")
    @Produces({MediaType.APPLICATION_JSON})
    public HurricaneSimulationEnsemble getHurricaneByCategory(@HeaderParam("X-Credential-Username") String username,
                                                    @PathParam("coast") String coast,
                                                    @QueryParam("category") int category,
                                                    @QueryParam("TransD") double transD, @QueryParam("LandfallLoc") IncorePoint landfallLoc,
                                                    @DefaultValue("6") @QueryParam("resolution") int resolution,
                                                    @DefaultValue("80") @QueryParam("gridPoints") int gridPoints) {

        //TODO: Find a way to include exception reason in HTTP Response INCORE-461
        //TODO: Handle both cases Sandy/sandy. Standardize to lower case?
        if(coast == null || category <=0 || category > 5){
            throw new NotFoundException("Coast needs to be gulf, florida or east. Category should be between 1 to 5");
        }

        if(HurricaneUtil.categoryMapping.get(coast) != null ){
            return getHurricane(username, transD, landfallLoc, HurricaneUtil.categoryMapping.get(coast)[category-1], resolution, gridPoints);
        }
        else{
            throw new NotFoundException("Error finding a mapping for the coast and category");
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public HurricaneSimulationEnsemble getHurricane(@HeaderParam("X-Credential-Username") String username,
                                             @QueryParam("TransD") double transD, @QueryParam("LandfallLoc") IncorePoint landfallLoc,
                                             @QueryParam("model") String model,
                                             @DefaultValue("6") @QueryParam("resolution") int resolution,
                                            @DefaultValue("80") @QueryParam("gridPoints") int gridPoints) {
        //TODO: Comeup with a better name for gridPoints
        //TODO: Can resolution be double? It's being hardcoded in Grid calculation
        //This function simulates wind fields for the selected data-driven model

        Hurricane hurricane = hurricaneRepo.getHurricaneByModel(model);
        JSONObject params = hurricane.getHurricaneParameters();

        // May be it's better to use List
        List<String> Vt0New = (List<String>) params.get("VTs_o_new");
        List<String> times = (List<String>) params.get("times");
        long longLandfall = (long)params.get("index_landfall");

        int indexLandfall = toIntExact(longLandfall) - 1 ; //Converting mat index to java
        //IncorePoint landfallPoint = landfallLoc.getLocation();
        List<Double> timeNewRadii = (List<Double>) params.get("time_radii_new");

        ComplexFormat cf = new ComplexFormat("j");
        Complex vt0 =  cf.parse(Vt0New.get(0));

        double x1 = vt0.getReal();
        double y1 = vt0.getImaginary();

        double ab = vt0.abs();
        double si = sin(toRadians(transD));
        double co = cos(toRadians(transD));

        double x2 = ab*si;
        double y2 = ab*co;

        double th = atan2(x1*y2-y1*x2,x1*x2+y1*y2);

        List<Complex> VTsSimu = new ArrayList<Complex>();

        for(String s: Vt0New){
            Complex c1 = cf.parse(s);
            double abNew = c1.abs();
            double angle = c1.getArgument();

            VTsSimu.add(HurricaneUtil.polar(abNew, angle+th));
        }

        List<IncorePoint> track = HurricaneUtil.locateNewTrack(timeNewRadii ,VTsSimu, landfallLoc, indexLandfall);

        if(!model.toLowerCase().equals("isabel") && !model.toLowerCase().equals("frances")){
            VTsSimu.remove(indexLandfall);
            track.remove(indexLandfall);
        }

        JSONArray para = (JSONArray) params.get("para");
        JSONArray omegaFitted = (JSONArray) params.get("omega_miss_fitted");
        JSONArray radiusM = (JSONArray) params.get("Rs");
        JSONArray zonesFitted = (JSONArray) params.get("contouraxis_zones_fitted");

        int paras = para.size();

        List<Complex[][]> VsTotal = new ArrayList<>();
        List<List<Double>> gridLatis = new ArrayList<>();
        List<List<Double>> gridLongs = new ArrayList<>();
        List<String> absTime = new ArrayList<>();

        List<HurricaneSimulation> hSimulations = new ArrayList<>();
        List<String> centers = new ArrayList<>();
        List<String> centerVel = new ArrayList<>();

        for(int i=0; i<paras; i++){
            HurricaneSimulation hsim = new HurricaneSimulation();
            hsim.setAbsTime(times.get(i));


            HurricaneGrid hgrid = HurricaneUtil.defineGrid(track.get(i), resolution, gridPoints);

            hsim.setGridLats(hgrid.getLati());
            hsim.setGridLongs(hgrid.getLongi());

            Complex[][] vsFinal = HurricaneCalc.simulateWindfieldWithCores((JSONObject) para.get(i), hgrid, VTsSimu.get(i),
                (JSONArray) omegaFitted.get(i), (JSONArray) zonesFitted.get(i), (JSONArray)radiusM.get(i));


            //hsim.setSurfaceVelocity(HurricaneUtil.convert2DComplexArrayToStringList(vsFinal));
            hsim.setSurfaceVelocityAbs(HurricaneUtil.convert2DComplexArrayToAbsList(vsFinal));
            hSimulations.add(hsim);


            IncorePoint ctr = hgrid.getCenter();
            centers.add(ctr.toString());
            int lonPos = hgrid.getLongi().indexOf(ctr.getLocation().getX()); //col
            int latPos = hgrid.getLati().indexOf(ctr.getLocation().getY()); //row

            //Use this is we want to display the velocity at the center point.
            //double centerVelocity = hsim.getSurfaceVelocityAbs().get(latPos).get(lonPos);
            //String centerVelocity = hsim.getSurfaceVelocity().get(latPos-1).get(lonPos);
            //centerVel.add(centerVelocity);
        }

        HurricaneSimulationEnsemble hEnsemble = new HurricaneSimulationEnsemble();
        hEnsemble.setResolution(resolution);
        hEnsemble.setTransD(transD);
        hEnsemble.setLandfallLocation(landfallLoc.toString());
        hEnsemble.setModelUsed(model);
        hEnsemble.setTimes(times);
        hEnsemble.setCenters(centers);
        hEnsemble.setCenterVelocities(centerVel);
        hEnsemble.setHurricaneSimulations(hSimulations);
       return  hEnsemble;


    }

}
