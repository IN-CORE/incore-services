/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.hazard.geotools;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import javax.ws.rs.NotFoundException;

import static java.lang.Math.sqrt;


/**
 * Created by ywkim on 09/17/2018.
 */
public class GeotoolsUtils {
    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final Logger logger = Logger.getLogger(GeotoolsUtils.class);
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    // file path for land polygon - dissolved
    public static String dslvPolygon = "tm_north_america_dislvd.shp";
    // file path for country boundary polygon - separated
    public static String sprPolygon = "tm_north_america_country.shp";
    public static SimpleFeatureCollection continentFeatures;
    public static SimpleFeatureCollection countriesFeatures;
    public static SpatialIndexFeatureCollection continentFeatureIndex;
    public static DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
    public static double searchDistLimit = 0;
    public static double minSearchDist = 0;


    //Is it a good idea to load them in static context? Affecting Performance when loading per request (each loop)
    static {
        try {
            continentFeatures = GetSimpleFeatureCollectionFromPath(dslvPolygon);
            countriesFeatures = GetSimpleFeatureCollectionFromPath(sprPolygon);
            continentFeatureIndex = new SpatialIndexFeatureCollection(continentFeatures.getSchema());
            continentFeatureIndex.addAll(continentFeatures);
            searchDistLimit = continentFeatureIndex.getBounds().getSpan(0);
            minSearchDist = searchDistLimit + 1.0e-6;
        } catch (IOException e) {
            throw new NotFoundException("Shapefile Not found");
        }
    }

    public static GeodeticCalculator geodeticCalculator = new GeodeticCalculator(crs);



    /**
     * create SimpleFeatureCollection from resource name
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection GetSimpleFeatureCollectionFromPath(String fileName) throws IOException{
        SimpleFeatureCollection sfc = null;

        URL fileUrl = GeotoolsUtils.class.getResource("/hazard/hurricane/shapefiles/" + fileName);
        //URL fileUrl = new URL("file:" + filePath);

        DataStore dataStore = getShapefileDataStore(fileUrl, true);
        FileDataStore fileDataStore = (FileDataStore) dataStore;
        SimpleFeatureSource sfs = fileDataStore.getFeatureSource();
        sfc = sfs.getFeatures();

        return sfc;
    }

    public static double CalcShortestDistanceFromPointToFeatures(SpatialIndexFeatureCollection featureIndex, double lat, double lon, GeodeticCalculator gc, DefaultGeographicCRS crs, double searchDistLimit, double minDist) {
        Coordinate minDistCoord = null;
        Coordinate pCoord = new Coordinate(lon, lat);
        ReferencedEnvelope refEnv = new ReferencedEnvelope(new Envelope(pCoord),
                featureIndex.getSchema().getCoordinateReferenceSystem());
        refEnv.expandBy(searchDistLimit);
        BBOX bbox = ff.bbox(ff.property(featureIndex.getSchema().getGeometryDescriptor().getName()), (BoundingBox) refEnv);
        SimpleFeatureCollection sfc = featureIndex.subCollection(bbox);
        SimpleFeatureIterator sfi = sfc.features();

        try {
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                LocationIndexedLine tempLine = new LocationIndexedLine(((MultiPolygon) sf.getDefaultGeometry()).getBoundary());
                LinearLocation snapPoint = tempLine.project(pCoord);
                Coordinate tempCoord = tempLine.extractPoint(snapPoint);
                double distance = tempCoord.distance(pCoord);
                if (distance < minDist) {
                    minDist = distance;
                    minDistCoord = tempCoord;
                }
            }
        } finally {
            sfi.close();
        }

        Point minTouchedPoint = null;

        if (minDistCoord == null) {
            minTouchedPoint = geometryFactory.createPoint((Coordinate) null);
        } else {
            minTouchedPoint = geometryFactory.createPoint(minDistCoord);
        }


        if (!minTouchedPoint.isEmpty()) {
            // calculation distance between the minTouchedPoint and input point
            try {
                gc.setStartingPosition(JTS.toDirectPosition(minTouchedPoint.getCoordinate(), crs));
                gc.setDestinationPosition(JTS.toDirectPosition(pCoord, crs));
            } catch (TransformException e) {
                e.printStackTrace();
            }

            double distance = gc.getOrthodromicDistance();

            double totalmeters = distance;
            double km = totalmeters / 1000;
            double meters = totalmeters - (km * 1000);
            double remaining_cm = distance - totalmeters * 10000;
            remaining_cm = Math.round(remaining_cm);
            double cm = remaining_cm / 100;

            return km;

        } else {
            return 0;
        }
    }


    /**
     * find shortest distance to the feature boundary from the point
     *
     * @param inFeatures
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static double FindShortestDistanceFromPointToFeatures(SimpleFeatureCollection inFeatures, double lat, double lon) throws IOException {
        SpatialIndexFeatureCollection featureIndex;
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
        featureIndex = new SpatialIndexFeatureCollection(inFeatures.getSchema());
        featureIndex.addAll(inFeatures);
        GeodeticCalculator gc = new GeodeticCalculator(crs);

        final double searchDistLimit = featureIndex.getBounds().getSpan(0);
        // give enough distance for the minimum distance to start
        double minDist = searchDistLimit + 1.0e-6;

        return CalcShortestDistanceFromPointToFeatures(featureIndex, lat, lon, gc, crs, searchDistLimit, minDist);
    }

    /**
     * find a field value of the feature using the point location
     *
     * @param inFeatures
     * @param fieldName
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static String getUnderlyingFieldValueFromPoint(SimpleFeatureCollection inFeatures, String fieldName, double lat, double lon) throws IOException{
        SimpleFeature touchedFeature = null;
        Coordinate coord = new Coordinate(lon, lat);
        Point point = geometryFactory.createPoint(coord);
        String outValue = "";

        if (inFeatures != null) {
            SimpleFeatureIterator sfi = inFeatures.features();
            try {
                while(sfi.hasNext()) {
                    SimpleFeature feature = sfi.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    if (geom.contains(point)) {
                        touchedFeature = feature;
                    }
                }
            } finally {
                sfi.close();
            }
        }

        String ret = "";
        if (touchedFeature != null) {
            String attribute = (String) touchedFeature.getAttribute(fieldName);
            if (attribute != null) {
                if(attribute.isEmpty()) {
                    ret = outValue;
                } else {
                    ret = attribute;
                }
            }
        }

        return ret.toLowerCase();
    }


    /**
     * find if the point is located in the polygon feature
     *
     * @param inFeatures
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static boolean isPointInPolygon(SimpleFeatureCollection inFeatures, double lat, double lon) throws IOException{
        boolean isContained = false;
        if (inFeatures != null) {
            Coordinate coord = new Coordinate(lon, lat);
            Point point = geometryFactory.createPoint(coord);
            SimpleFeatureIterator sfi = inFeatures.features();
            try {
                while(sfi.hasNext()) {
                    SimpleFeature feature = sfi.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    isContained = geom.contains(point);
                }
            } finally {
                sfi.close();
            }
        }

        return isContained;
    }

    /**
     * get shapefile data store
     *
     * @param shpUrl
     * @param spatialIndex
     * @return
     * @throws IOException
     */
    public static DataStore getShapefileDataStore(URL shpUrl, Boolean spatialIndex) throws IOException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", shpUrl);
        map.put("create spatial index", spatialIndex);
        map.put("enable spatial index", spatialIndex);

        return DataStoreFinder.getDataStore(map);
    }

    /**
     * main for testing
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // file path for land polygon
        String dslvPolygon = "tm_north_america_dislvd.shp";
        // file path for country boundary polygon
        String sprPolygon = "tm_north_america_country.shp";


        SimpleFeatureCollection dslvFeatures = GetSimpleFeatureCollectionFromPath(dslvPolygon);
        SimpleFeatureCollection sprFeatures = GetSimpleFeatureCollectionFromPath(sprPolygon);

        // lat, lon value
        double lat = 21.378178;
        double lon = 87.925500;

        // check if the point is on the land
        boolean isContained = true;
        //boolean isContained = isPointInPolygon(dslvFeatures, lat, lon);
        //System.out.println(isContained);

        if (isContained) {
            // if it is on the land, get the country name
            String name = getUnderlyingFieldValueFromPoint(sprFeatures, "NAME", lat, lon);
            System.out.println(name);

            // get shortest km distance to coastal line
            double shortestDist =  FindShortestDistanceFromPointToFeatures(dslvFeatures, lat, lon);
            System.out.println(shortestDist);

            ////////////////////////////////////////
            // new method for faster iteration
            ////////////////////////////////////////
            SpatialIndexFeatureCollection featureIndex;
            DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
            featureIndex = new SpatialIndexFeatureCollection(dslvFeatures.getSchema());
            featureIndex.addAll(dslvFeatures);
            GeodeticCalculator gc = new GeodeticCalculator(crs);

            final double searchDistLimit = featureIndex.getBounds().getSpan(0);
            // give enough distance for the minimum distance to start
            double minDist = searchDistLimit + 1.0e-6;

            shortestDist = CalcShortestDistanceFromPointToFeatures(featureIndex, lat, lon, gc, crs, searchDistLimit, minDist);
            System.out.println(shortestDist);
        }


    }
}

