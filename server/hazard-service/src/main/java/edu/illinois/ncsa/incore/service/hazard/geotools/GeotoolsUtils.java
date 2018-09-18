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

/**
 * Created by ywkim on 09/17/2018.
 */
public class GeotoolsUtils {
    static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final Logger logger = Logger.getLogger(GeotoolsUtils.class);

    /**
     * create SimpleFeatureCollection from file path
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection GetSimpleFeatureCollectionFromPath(String filePath) throws IOException{
        SimpleFeatureCollection sfc = null;
        URL fileUrl = new URL("file:" + filePath);

        DataStore dataStore = getShapefileDataStore(fileUrl, false);
        FileDataStore fileDataStore = (FileDataStore) dataStore;
        SimpleFeatureSource sfs = fileDataStore.getFeatureSource();
        sfc = sfs.getFeatures();

        return sfc;
    }

    /**
     * find shortest distance to the feature boundary from the point
     *
     * @param filePath
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static double FindShortestDistancePointFromFeatures(String filePath, double lat, double lon) throws IOException {
        SimpleFeatureCollection inFeatures = GetSimpleFeatureCollectionFromPath(filePath);
        if (inFeatures != null) {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            SpatialIndexFeatureCollection featureIndex;
            SimpleFeature touchedFeature = null;
            Coordinate coord = new Coordinate(lon, lat);
            Point startPoint = geometryFactory.createPoint(coord);
            DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;

            featureIndex = new SpatialIndexFeatureCollection(inFeatures.getSchema());
            featureIndex.addAll(inFeatures);

            final double searchDistLimit = featureIndex.getBounds().getSpan(0);
            Coordinate pCoord = startPoint.getCoordinate();
            ReferencedEnvelope refEnv = new ReferencedEnvelope(new Envelope(pCoord),
                    featureIndex.getSchema().getCoordinateReferenceSystem());
            refEnv.expandBy(searchDistLimit);
            BBOX bbox = ff.bbox(ff.property(featureIndex.getSchema().getGeometryDescriptor().getName()), (BoundingBox) refEnv);
            SimpleFeatureCollection sfc = featureIndex.subCollection(bbox);

            // check if the point is in the land

            // give enough distance for the minimum distance to start
            double minDist = searchDistLimit + 1.0e-6;

            Coordinate minDistPoint = null;
            SimpleFeatureIterator sfi = sfc.features();
            try {
                while (sfi.hasNext()) {
                    SimpleFeature sf = sfi.next();
                    LocationIndexedLine tempLine = new LocationIndexedLine(((MultiPolygon) sf.getDefaultGeometry()).getBoundary());
                    LinearLocation snapPoint = tempLine.project(pCoord);
                    Coordinate tempPoint = tempLine.extractPoint(snapPoint);
                    double distance = tempPoint.distance(pCoord);
                    if (distance < minDist) {
                        minDist = distance;
                        minDistPoint = tempPoint;
                        touchedFeature = sf;
                    }
                }
            } finally {
                sfi.close();
            }


            Point minTouchedPoint = null;

            if (minDistPoint == null) {
                minTouchedPoint = geometryFactory.createPoint((Coordinate) null);
            } else {
                minTouchedPoint = geometryFactory.createPoint(minDistPoint);
            }


            if (!minTouchedPoint.isEmpty()) {
                // calculation distance between the minTouchedPoint and input point
                GeodeticCalculator gc = new GeodeticCalculator(crs);
                try {
                    gc.setStartingPosition(JTS.toDirectPosition(minTouchedPoint.getCoordinate(), crs));
                    gc.setDestinationPosition(JTS.toDirectPosition(startPoint.getCoordinate(), crs));
                } catch (TransformException e) {
                    // TODO Auto-generated catch block
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
        } else {
            return 0;
        }
    }

    /**
     * find a field value of the feature using the point location
     *
     * @param filePath
     * @param fieldName
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static String getUnderlyingFieldValueFromPoint(String filePath, String fieldName, double lat, double lon) throws IOException{
        SimpleFeatureCollection inFeatures = GetSimpleFeatureCollectionFromPath(filePath);
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

        if (touchedFeature != null) {
            String attribute = (String) touchedFeature.getAttribute(fieldName);
            if (attribute != null) {
                if(attribute.isEmpty()) {
                    return outValue;
                } else {
                    return attribute;
                }
            }
        }

        return outValue;
    }


    /**
     * find if the point is located in the polygon feature
     *
     * @param filePath
     * @param lat
     * @param lon
     * @return
     * @throws IOException
     */
    public static boolean isPointInPolygon(String filePath, double lat, double lon) throws IOException{
        boolean isContained = false;
        SimpleFeatureCollection inFeatures = GetSimpleFeatureCollectionFromPath(filePath);
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
        String dslvPolygon = "hazard-service\\src\\main\\data\\hurricane\\tm_north_america_dislvd.shp";
        // file path for country boundary polygon
        String sprPolygon = "hazard-service\\src\\main\\data\\hurricane\\tm_north_america_country.shp";

        // lat, lon value
        double lat = 41.378178;
        double lon = -87.925500;

        // check if the point is on the land
        boolean isContained = isPointInPolygon(dslvPolygon, lat, lon);
        System.out.println(isContained);

        if (isContained) {
            // if it is on the land, get the country name
            String name = getUnderlyingFieldValueFromPoint(sprPolygon, "NAME", lat, lon);
            System.out.println(name);

            // get shortest km distance to coastal line
            double shortestDist =  FindShortestDistancePointFromFeatures(dslvPolygon, lat, lon);
            System.out.println(shortestDist);
        }

    }
}

