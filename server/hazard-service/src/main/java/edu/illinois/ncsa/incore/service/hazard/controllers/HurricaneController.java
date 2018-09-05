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
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
//import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.WindHazardResult;
//import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
//import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoUtils;
//import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneGrid;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject; //TODO: org.json.* vs org.json.simple.*
// http://www.rojotek.com/blog/2009/05/07/a-review-of-5-java-json-libraries/

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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
    @Produces({MediaType.APPLICATION_JSON})
    public void getHurricane(@HeaderParam("X-Credential-Username") String username,
                                             @QueryParam("TransD") int transD, @QueryParam("LandfallLoc") String landfallLoc,
                                             @QueryParam("model") String model,
                                             @DefaultValue("6") @QueryParam("resolution") int resolution) {
        //TODO: Can resolution be double? It's being hardcoded in Grid calculation.

        //This function simulates wind fields for the selected data-driven model

        Hurricane hurricane = hurricaneRepo.getHurricaneByModel(model);
        //getHurricaneByModel(model)
        JSONObject params = hurricane.getHurricaneParameters();

        // May be it's better to use List
        List<String> Vt0New = (List<String>) params.get("VTs_o_new");
        long longLandfall = (long)params.get("index_landfall");

        int indexLandfall = toIntExact(longLandfall) - 1 ; //Converting mat index to java
        IncorePoint landfallPoint = new IncorePoint(landfallLoc);
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

            VTsSimu.add(polar(abNew, angle+th));
        }

        List<IncorePoint> track = locateNewTrack(timeNewRadii ,VTsSimu, landfallPoint, indexLandfall);

        JSONArray para = (JSONArray) params.get("para");
        JSONArray omegaFitted = (JSONArray) params.get("omega_miss_fitted");
        JSONArray radiusM = (JSONArray) params.get("rconv");
        JSONArray zonesFitted = (JSONArray) params.get("contouraxis_zones_fitted");

        int paras = para.size();

        ArrayList<HurricaneGrid> grids = new ArrayList<HurricaneGrid>();

        for(int i=0; i<paras; i++){
            HurricaneGrid hgrid = defineGrid(track.get(i), resolution);
            simulateWindfieldWithCores((JSONObject) para.get(i), hgrid, VTsSimu.get(i),
                (JSONArray) omegaFitted.get(i), (JSONArray) zonesFitted.get(i), radiusM);

        }
        int p=0;

    }

    //TODO: Move all these functions to HurricaneUtils

    private static Complex polar(double rho, double theta) {
        return (new Complex(rho * Math.cos(theta), rho * Math.sin(theta)));
    }

    private static List<IncorePoint> locateNewTrack(List<Double> timeNewRadii ,List<Complex> VTsSimu, IncorePoint LandfallLoc, int indexLandfall){

        List<IncorePoint> centers = new ArrayList<IncorePoint>(indexLandfall+1);

        int timesSize = 0;
        timesSize = timeNewRadii.size();

        List<IncorePoint> centersSimu = new ArrayList<IncorePoint>(timesSize);

        centers.add(LandfallLoc);

        for(int i=indexLandfall; i >= 1 ; i--){
            double deltaT = 0;
            deltaT = (timeNewRadii.get(i) - timeNewRadii.get(i-1))* 3600;
            Complex deltaDist = VTsSimu.get(i-1).multiply(deltaT);
            centers.add(0, convertXY2LongLati(centers.get(0), deltaDist, true, "hwind"));
        }

        centersSimu.add(centers.get(0));

        for(int i = 1; i < timesSize; i++){
            double deltaT = 0;
            deltaT = (timeNewRadii.get(i) - timeNewRadii.get(i-1))* 3600;
            Complex deltaDist = VTsSimu.get(i-1).multiply(deltaT);
            centersSimu.add(convertXY2LongLati(centersSimu.get(i-1), deltaDist, false, "hwind"));
        }
        return centersSimu;
    }

    private static IncorePoint convertXY2LongLati(IncorePoint center, Complex deltaDist, boolean backwards, String method){
        final double R =6371*1000; // mean radius of earth, meter
        final double para = 3.5;

        double centerLong = center.getLocation().getX();
        double centerLati = center.getLocation().getY();

        double x = deltaDist.getReal();
        double y = deltaDist.getImaginary();

        if(backwards){
            x = -x;
            y = -y;
        }

        double c = abs(y)/R;
        double a = pow(tan(c/2), 2)/(1+pow((tan(c/2)),2));


        double deltaLati = 2*asin(sqrt(a))/Math.PI*180; //degree
        double lati= 0;
        lati= centerLati + (deltaLati* signum(y));

        double c1 = abs(x)/R;
        double a1 = pow(tan(c1/2), 2)/(1+pow((tan(c1/2)),2));

        double deltaLong = 0;
        double longi = 0;

        // TODO:Precision difference of .001 in mat lab vs java calculation. Due to default rounding that was applied.
        //Only in long calculation.
        //Ex. -79.2014 from matlab is calculated as -79.202741231313
        if(method == "hwind"){
             deltaLong = 2 * asin(sqrt(a1/(cos(para/(Math.PI*180))))) / Math.PI*180;
             longi = centerLong + deltaLong*signum(x);
        }
        else if(method == "real"){ //Migrate these if needed in future calculations.

        }
        else if(method == "linear1"){

        }
        else if(method == "linear1"){

        }

        return new IncorePoint(lati+","+longi);
    }

    private static IncorePoint convertXY2LongLati( IncorePoint center, Complex deltaDist, boolean backwards){
        return convertXY2LongLati(center,deltaDist, backwards,"hwind");
    }

    private static HurricaneGrid defineGrid(IncorePoint center, int resolution){

        HurricaneGrid g = new HurricaneGrid();
        double centerLong = center.getLocation().getX();
        double centerLati = center.getLocation().getY();

        double xMax = 6 * 80; //TODO: Should 6 come from resolution?
        //double xM = xMax*1000;

       List<Double> xsIntermediate = new ArrayList<Double>();
       List<Double> xs = new ArrayList<Double>();
       List<Double> ys = new ArrayList<Double>();

        int i = 0;
        for(double x= -xMax; x <= xMax;x= x+resolution){
            xsIntermediate.add(x);
            i++;
        }
        int cords = xsIntermediate.size();

        for(int j=0; j < cords; j++ ){
            for(int k=0; k< cords; k++) {
                xs.add(xsIntermediate.get(j)*1000);
                ys.add(xsIntermediate.get(k)*1000);
            }
        }

        List<Double> latis = new ArrayList<Double>();
        List<Double> longis = new ArrayList<Double>();

        int xyCords = xs.size();

        for(int j=0; j< xyCords; j++){
            IncorePoint p = convertXY2LongLati(center, new Complex(xs.get(j), ys.get(j)), false);
            latis.add(p.getLocation().getY());
            longis.add(p.getLocation().getX());
        }

        List<Double> lati = new ArrayList<Double>();
        List<Double> longi = new ArrayList<Double>();

        for(int j=0; j<cords;j++){
            lati.add(latis.get(j));
            longi.add(longis.get(j*cords));
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

    private static void simulateWindfieldWithCores(JSONObject para, HurricaneGrid grid, Complex VTs,
                                                    JSONArray omegaFitted, JSONArray zonesFitted, JSONArray radiusM){
        //Add functionaility to Access Maps
        double rho = 1.2754; // air density, kg/m^3
        double mb2Pa = 100; // unit change from mb to Pa
        double pn = 1013.25*mb2Pa; // Pa, ambient pressure, 101.325 kPa, 29.921 inHg, 760 mmHg.
        double kt2ms = 0.514444; // unit change from KT to m/s
        double fr = 0.8; //Conversion parameter

        ArrayList<Double> thetaRadians = new ArrayList<Double>();

        for(double i=0; i <=360; i = i+ 0.1){
            thetaRadians.add(i*Math.PI/180);
        }

        double bInner = (double)para.get("b_inner");
        double bOuter = (double)para.get("b_outer");
        double fCorolosis = (double)para.get("f");

        JSONArray rmThetaVspInner = (JSONArray) para.get("rm_theta_vsp_inner");
        JSONArray rmThetaVspOuter = (JSONArray) para.get("rm_theta_vsp_outer");
        double pc = (double)para.get("pc"); //central Pressure
        JSONArray vgInner = (JSONArray) para.get("vg_inner");
        JSONArray vgOuter = (JSONArray) para.get("vg_outer");

        double rm = (double)para.get("rm_outer"); // inner is not being used at all. why?

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
        }

    }


}
