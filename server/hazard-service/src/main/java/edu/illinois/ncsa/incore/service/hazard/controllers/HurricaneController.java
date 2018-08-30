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
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.json.simple.JSONObject; //TODO: org.json.* vs org.json.simple.*
// http://www.rojotek.com/blog/2009/05/07/a-review-of-5-java-json-libraries/

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

        //This function simulates wind fields for the selected data-driven model

        Hurricane hurricane = hurricaneRepo.getHurricaneByModel(model);
        //getHurricaneByModel(model)
        JSONObject params = hurricane.getHurricaneParameters();

        ArrayList<String> Vt0New = (ArrayList<String>) params.get("VTs_o_new");
        long longLandfall = (long)params.get("index_landfall");

        int indexLandfall = toIntExact(longLandfall) - 1 ; //Converting mat index to java
        IncorePoint landfallPoint = new IncorePoint(landfallLoc);
        ArrayList<Double> timeNewRadii = (ArrayList<Double>) params.get("time_radii_new");

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

        ArrayList<Complex> VTsSimu = new ArrayList<Complex>();

        for(String s: Vt0New){
            Complex c1 = cf.parse(s);
            double abNew = c1.abs();
            double angle = c1.getArgument();

            VTsSimu.add(polar(abNew, angle+th));
        }

        ArrayList<IncorePoint> track = locateNewTrack(timeNewRadii ,VTsSimu, landfallPoint, indexLandfall);
        int j= 1;


        }

    //TODO: Move all these functions to HurricaneUtils

    private static Complex polar(double rho, double theta) {
        return (new Complex(rho * Math.cos(theta), rho * Math.sin(theta)));
    }

    private static ArrayList<IncorePoint> locateNewTrack(ArrayList<Double> timeNewRadii ,ArrayList<Complex> VTsSimu, IncorePoint LandfallLoc, int indexLandfall){

        ArrayList<IncorePoint> centers = new ArrayList<IncorePoint>(indexLandfall+1);

       //centers.

        centers.add(LandfallLoc);

        for(int i=indexLandfall; i >= 1 ; i--){
            double deltaT = 0;
            deltaT = (timeNewRadii.get(i) - timeNewRadii.get(i-1))* 3600;
            Complex deltaDist = VTsSimu.get(i).multiply(deltaT);
            centers.add(0, convertXY2LongLati(centers.get(0), deltaDist, "hwind"));
        }

        return centers;
    }

    private static IncorePoint convertXY2LongLati(IncorePoint center, Complex deltaDist, String method){
        final double R =6371*1000; // mean radius of earth, meter
        final double para = 3.5;

        double centerLong = center.getLocation().getX();
        double centerLati = center.getLocation().getY();

        double x = deltaDist.getReal();
        double y = deltaDist.getImaginary();

        double c = abs(y)/R;
        double a = pow(tan(c/2), 2)/(1+pow((tan(c/2)),2));


        double deltaLati = 2*asin(sqrt(a))/Math.PI*180; //degree
        double lati= 0;
        lati= centerLati + (deltaLati* signum(y));

        double c1 = abs(x)/R;
        double a1 = pow(tan(c1/2), 2)/(1+pow((tan(c1/2)),2));

        double deltaLong = 0;
        double longi = 0;

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

    private static IncorePoint convertXY2LongLati( IncorePoint center, Complex deltaDist){
        return convertXY2LongLati(center,deltaDist, "hwind");
    }



}
