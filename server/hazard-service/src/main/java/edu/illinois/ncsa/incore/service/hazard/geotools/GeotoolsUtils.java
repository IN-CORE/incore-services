/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.geotools;


import org.apache.log4j.Logger;
import org.geotools.data.*;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.*;


/**
 * Created by ywkim on 09/17/2018.
 */
public class GeotoolsUtils {
    private static final Logger logger = Logger.getLogger(GeotoolsUtils.class);
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

    /**
     * create SimpleFeatureCollection from resource name
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection GetSimpleFeatureCollectionFromPath(String filePath) throws IOException {
        SimpleFeatureCollection sfc = null;
        URL fileUrl = new URL("file:" + filePath);

        DataStore dataStore = getShapefileDataStore(fileUrl, true);
        FileDataStore fileDataStore = (FileDataStore) dataStore;
        SimpleFeatureSource sfs = fileDataStore.getFeatureSource();
        sfc = sfs.getFeatures();

        return sfc;
    }

    public static Point CalcTangentPointToFeatures(Object polyObj, double lat, double lon, GeodeticCalculator gc,
                                                   DefaultGeographicCRS crs, double searchDistLimit, double minDist) {
        Coordinate minDistCoord = null;
        Coordinate pCoord = new Coordinate(lon, lat);
        if (polyObj instanceof SpatialIndexFeatureCollection) {
            SpatialIndexFeatureCollection featureIndex = (SpatialIndexFeatureCollection) polyObj;
            ReferencedEnvelope refEnv = new ReferencedEnvelope(new Envelope(pCoord),
                featureIndex.getSchema().getCoordinateReferenceSystem());
            refEnv.expandBy(searchDistLimit);
            BBOX bbox = ff.bbox(ff.property(featureIndex.getSchema().getGeometryDescriptor().getName()), refEnv);
            SimpleFeatureCollection sfc = featureIndex.subCollection(bbox);
            SimpleFeatureIterator sfi = sfc.features();

            try {
                while (sfi.hasNext()) {
                    SimpleFeature sf = sfi.next();
                    LocationIndexedLine tempLine = null;
                    tempLine = new LocationIndexedLine(((MultiPolygon) sf.getDefaultGeometry()).getBoundary());

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
        } else if (polyObj instanceof Polygon) {
            Polygon poly = (Polygon) polyObj;
            Coordinate[] polyCords = poly.getCoordinates();
            LocationIndexedLine tempLine = new LocationIndexedLine(poly.getBoundary());

            LinearLocation snapPoint = tempLine.project(pCoord);
            Coordinate tempCoord = tempLine.extractPoint(snapPoint);
            double distance = tempCoord.distance(pCoord);
            if (distance < minDist) {
                minDist = distance;
                minDistCoord = tempCoord;
            }
        }

        Point minTouchedPoint = null;

        if (minDistCoord == null) {
            minTouchedPoint = geometryFactory.createPoint((Coordinate) null);
        } else {
            minTouchedPoint = geometryFactory.createPoint(minDistCoord);
        }

        return minTouchedPoint;
    }


    public static double CalcShortestDistanceFromPointToFeatures(Object polyObj, double lat, double lon, GeodeticCalculator gc,
                                                                 DefaultGeographicCRS crs, double searchDistLimit, double minDist) {
        Coordinate pCoord = new Coordinate(lon, lat);

        Point minTouchedPoint = CalcTangentPointToFeatures(polyObj, lat, lon, gc, crs, searchDistLimit, minDist);

        if (!minTouchedPoint.isEmpty()) {
            // calculation distance between the minTouchedPoint and input point
            try {
                gc.setStartingPosition(JTS.toDirectPosition(minTouchedPoint.getCoordinate(), crs));
                gc.setDestinationPosition(JTS.toDirectPosition(pCoord, crs));
            } catch (TransformException e) {
                logger.error("touching point does not exist: " + e);
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
    public static String getUnderlyingFieldValueFromPoint(SimpleFeatureCollection inFeatures, String fieldName, double lat, double lon) throws IOException {
        SimpleFeature touchedFeature = null;
        Coordinate coord = new Coordinate(lon, lat);
        Point point = geometryFactory.createPoint(coord);
        String outValue = "";

        if (inFeatures != null) {
            SimpleFeatureIterator sfi = inFeatures.features();
            try {
                while (sfi.hasNext()) {
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
                if (attribute.isEmpty()) {
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
    public static boolean isPointInPolygon(SimpleFeatureCollection inFeatures, double lat, double lon) throws IOException {
        boolean isContained = false;
        if (inFeatures != null) {
            Coordinate coord = new Coordinate(lon, lat);
            Point point = geometryFactory.createPoint(coord);
            SimpleFeatureIterator sfi = inFeatures.features();
            try {
                while (sfi.hasNext()) {
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
    public static DataStore getShapefileDataStore(URL shpUrl, Boolean spatialIndex) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", shpUrl);
        map.put("create spatial index", spatialIndex);
        map.put("enable spatial index", spatialIndex);

        return DataStoreFinder.getDataStore(map);
    }

    public static void outToFile(File pathFile, SimpleFeatureType schema, DefaultFeatureCollection collection) throws IOException {
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", pathFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        params.put("enable spatial index", Boolean.TRUE);
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(schema);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();

            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();

            } finally {
                transaction.close();
            }

        } else {
            logger.error(typeName + " does not support read/write access");
            System.exit(1);
        }
    }

    /**
     * Creates a polygon from a file that contains lon,lat per line
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Polygon getPolygonFromFile(String filePath) throws IOException {

        GeometryFactory gf = new GeometryFactory();
        URL fileUrl = new URL("file:" + filePath);
        List<Coordinate> coordinates = new ArrayList<>();
        Polygon p = null;
        try {
            Scanner s = new Scanner(fileUrl.openStream());

            while (s.hasNext()) {
                String line = s.nextLine();
                String[] cords = line.split("\t");
                double lon = Double.parseDouble(cords[0]);
                double lat = Double.parseDouble(cords[1]);

                coordinates.add(new Coordinate(lon, lat));
            }

            p = gf.createPolygon(coordinates.toArray(new Coordinate[]{}));

            return p;

        } catch (Exception e) {
            logger.error("Failed to open file: " + e);
        }

        return p;
    }

//    /**
//     * main for testing to calculate the shortest distance
//     * @param args
//     * @throws IOException
//     */
//    public static void main(String[] args) throws IOException {
//        // file path for land polygon
////        String dslvPolygon = "tm_north_america_dislvd.shp";
//        // file path for country boundary polygon
////        String sprPolygon = "tm_north_america_country.shp";
//
//
//        SimpleFeatureCollection dslvFeatures = GetSimpleFeatureCollectionFromPath(dslvPolygon);
//        SimpleFeatureCollection sprFeatures = GetSimpleFeatureCollectionFromPath(sprPolygon);
//
//        // lat, lon value
//        double lat = 21.378178;
//        double lon = 87.925500;
//
//        // check if the point is on the land
//        boolean isContained = true;
//        //boolean isContained = isPointInPolygon(dslvFeatures, lat, lon);
//
//        if (isContained) {
//            // if it is on the land, get the country name
//            String name = getUnderlyingFieldValueFromPoint(sprFeatures, "NAME", lat, lon);
//
//            // get shortest km distance to coastal line
//            double shortestDist = FindShortestDistanceFromPointToFeatures(dslvFeatures, lat, lon);
//            logger.debug(shortestDist);
//
//            ////////////////////////////////////////
//            // new method for faster iteration
//            ////////////////////////////////////////
//            SpatialIndexFeatureCollection featureIndex;
//            featureIndex = new SpatialIndexFeatureCollection(dslvFeatures.getSchema());
//            featureIndex.addAll(dslvFeatures);
//            GeodeticCalculator gc = new GeodeticCalculator(crs);
//
//            final double searchDistLimit = featureIndex.getBounds().getSpan(0);
//            // give enough distance for the minimum distance to start
//            double minDist = searchDistLimit + 1.0e-6;
//
//            shortestDist = CalcShortestDistanceFromPointToFeatures(featureIndex, lat, lon, gc, crs, searchDistLimit, minDist);
//            logger.debug(shortestDist);
//        }
//    }
}

