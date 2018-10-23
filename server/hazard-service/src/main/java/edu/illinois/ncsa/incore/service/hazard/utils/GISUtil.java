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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static GridCoverage getGridCoverage(String datasetId, String creator) {
        File incoreWorkDirectory = ServiceUtil.getWorkDirectory();
        File file = ServiceUtil.getFileFromDataService(datasetId, creator, incoreWorkDirectory);

        URL inSourceFileUrl = null;
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(incoreWorkDirectory, fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                String fileExt = FilenameUtils.getExtension(newFile.getName());
                if (fileExt.equalsIgnoreCase("tif")) {
                    inSourceFileUrl = newFile.toURI().toURL();
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            logger.error("Error getting tif file from data service", e);
            return null;
        }

        final AbstractGridFormat format = new GeoTiffFormat();
        GridCoverage gridCoverage = null;
        GeoTiffReader reader = null;
        try {
            reader = new GeoTiffReader(inSourceFileUrl, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
            if (reader != null) {
                gridCoverage = reader.read(null);
            }
        } catch (DataSourceException e) {
            logger.error("Error creating tiff reader.", e);
        } catch (IOException e) {
            logger.error("Error reading grid coverage.", e);
        }
        return gridCoverage;
    }
}
