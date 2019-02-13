/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.utils;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GISUtil {
    private static final Logger logger = Logger.getLogger(GISUtil.class);

    public static SimpleFeature getPointInPolygon(Point point, SimpleFeatureCollection featureCollection) {
        SimpleFeature feature = null;
        boolean found = false;

        SimpleFeatureIterator geologyIterator = featureCollection.features();
        try {
            while (geologyIterator.hasNext() && !found) {
                // String featureId = featureIdIterator.next();
                SimpleFeature f = geologyIterator.next();

                Object polygonObject = f.getAttribute(0);
                if (polygonObject instanceof Polygon) {
                    Polygon polygon = (Polygon) polygonObject;
                    found = polygon.contains(point);
                    if (found) {
                        feature = f;
                    }
                } else {
                    MultiPolygon attribute = (MultiPolygon) polygonObject;
                    for (int i = 0; i < attribute.getNumGeometries(); i++) {
                        Polygon p = (Polygon) attribute.getGeometryN(i);

                        found = p.contains(point);
                        if (found) {
                            i = attribute.getNumGeometries();
                            feature = f;
                        }
                    }
                }
            }
        } finally {
            geologyIterator.close();
        }

        return feature;
    }

    public static URL unZipShapefiles(File file, File destDirectory){
        URL inSourceFileUrl = null;
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(destDirectory, fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                String fileExt = FilenameUtils.getExtension(newFile.getName());
                if (fileExt.equalsIgnoreCase("shp")) {
                    inSourceFileUrl = newFile.toURI().toURL();
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            logger.error("Error unzipping shapefile", e);
            return null;
        }
        return inSourceFileUrl;
    }

    public static FeatureCollection getFeatureCollection(String datasetId, String creator) {

        File incoreWorkDirectory = ServiceUtil.getWorkDirectory();
        File file = ServiceUtil.getFileFromDataService(datasetId, creator, incoreWorkDirectory);

        URL inSourceFileUrl = unZipShapefiles(file, incoreWorkDirectory);

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("url", inSourceFileUrl);

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
            dataStore.dispose();
            SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) source.getFeatures();
            return inputFeatures;
        } catch (IOException e) {
            logger.error("Error reading shapefile");
            return null;
        }

    }

    public static synchronized GridCoverage getGridCoverage(String datasetId, String creator) {


        URL inSourceFileUrl = null;

        try {
            //first see if the cache has the tif file in it
            File cacheDir = ServiceUtil.getCacheDirectory("dataset-" + datasetId);
            if (cacheDir.exists()) {
                String[] tifFiles = cacheDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".tif");
                    }
                });
                if (tifFiles != null && tifFiles.length > 0) {
                    inSourceFileUrl = new File(cacheDir, tifFiles[0]).toURI().toURL();
                }
            }


            //if not, download the cache it
            if (inSourceFileUrl == null) {
                inSourceFileUrl = cacheGridCoverage(datasetId, cacheDir, creator);
            }

            //if we still don't have it, there's a problem
            if (inSourceFileUrl == null) {
                logger.error("Could not locate grid coverage");
                return null;
            }

            final AbstractGridFormat format = new GeoTiffFormat();
            GridCoverage gridCoverage = null;
            GeoTiffReader reader;
            try {
                reader = new GeoTiffReader(inSourceFileUrl, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
                gridCoverage = reader.read(null);
            } catch (DataSourceException e) {
                logger.error("Error creating tiff reader.", e);
            } catch (IOException e) {
                logger.error("Error reading grid coverage.", e);
            }
            return gridCoverage;
        } catch (IOException e) {
            logger.error("Could not read cache directory", e);
        }
        return null;
    }

    private static URL cacheGridCoverage(String datasetId, File cacheDir, String creator) {
        File file = ServiceUtil.getFileFromDataService(datasetId, creator, cacheDir);

        byte[] buffer = new byte[1024];
        URL tifFile = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(cacheDir, fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                String fileExt = FilenameUtils.getExtension(newFile.getName());
                if (fileExt.equalsIgnoreCase("tif")) {
                    tifFile = newFile.toURI().toURL();
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            logger.error("Error getting tif file from data service", e);
            return null;
        }
        return tifFile;
    }


}
