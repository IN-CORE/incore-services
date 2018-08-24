/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.HazardConstants;
import edu.illinois.ncsa.incore.service.hazard.models.eq.HazardDataset;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
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
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Point2D;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Misc utility functions for doing conversion of hazard types and units
 */
public class HazardUtil {
    private static final Logger logger = Logger.getLogger(HazardUtil.class);
    public static final String HAZARD = "hazard";

    public static final String units_g = "g";
    public static final String units_cm = "cm";
    // Constant string expressions appearing throughout hazard code
    public static final String NONE = "None";
    public static final String PGA = "PGA";
    public static final String PGV = "PGV";
    public static final String PGD = "PGD";
    public static final String SA = "SA";
    public static final String SD = "SD";
    public static final String SV = "SV";
    public static final String LIQUEFACTION_PGD = "liquefactionPGD";
    private static final String units_percg = "%g";
    // Metric
    private static final String units_m = "meters";
    private static final String units_cms = "cm/s";
    private static final String units_ins = "in/s";
    // English
    public static final String units_in = "in";
    private static final String units_ft = "feet";
    private static final String sa_pgv = "sapgv";
    private static final String pga_pgd = "pgapgd";
    private static final String pga_pga = "pgapga";
    private static final String sa_sa = "sasa";
    private static final String sa_sv = "sasv";
    private static final String sa_sd = "sasd";
    private static final String sd_sd = "sdsd";
    private static final String pgv_pgv = "pgvpgv";
    private static final String FAULT_LENGTH = "Length";
    private static final String FAULT_WIDTH = "Width";
    public static double R_EARTH = 6373.0; // km
    // NEHRP Site Amplification
    // XXX: should this be a table dataset so it can be changed in the future?
    static double[][] short_period = {{0.8, 1.0, 1.2, 1.6, 2.5}, {0.8, 1.0, 1.2, 1.4, 1.7}, {0.8, 1.0, 1.1, 1.2, 1.2},
        {0.8, 1.0, 1.0, 1.1, 0.9}, {0.8, 1.0, 1.0, 1.0, 0.9}};
    static double[] shortPeriodIntervals = {0.25, 0.5, 0.75, 1.0, 1.25};
    static double[][] long_period = {{0.8, 1.0, 1.7, 2.4, 3.5}, {0.8, 1.0, 1.6, 2.0, 3.2}, {0.8, 1.0, 1.5, 1.8, 2.8},
        {0.8, 1.0, 1.4, 1.6, 2.4}, {0.8, 1.0, 1.3, 1.5, 2.4}};
    static double[] longPeriodIntervals = {0.1, 0.2, 0.3, 0.4, 0.5};

    public static int getSiteClassAsInt(String siteClass) {
        int siteClassInt = -1;
        if (siteClass.equalsIgnoreCase("A")) {
            siteClassInt = 0;
        } else if (siteClass.equalsIgnoreCase("B")) {
            siteClassInt = 1;
        } else if (siteClass.equalsIgnoreCase("C")) {
            siteClassInt = 2;
        } else if (siteClass.equalsIgnoreCase("D")) {
            siteClassInt = 3;
        } else if (siteClass.equalsIgnoreCase("E")) {
            siteClassInt = 4;
        } else if (siteClass.equalsIgnoreCase("F")) {
            siteClassInt = 5;
        }

        return siteClassInt;
    }

    public static double convertHazard(double hazard, String fromUnits, double t, String fromHazardType, String toUnits,
                                       String toHazardType) {
        double hazardVal = convertHazardType(hazard, fromUnits, t, fromHazardType, toUnits, toHazardType);
        if (Double.isNaN(hazardVal)) {
            hazardVal = 0.0;
        }
        // not really sure that we should use 0.0 for infinite values, but we
        // know there's no way we want them in our actual values
        if (Double.isInfinite(hazardVal)) {
            hazardVal = 0.0;
        }
        return hazardVal;

    }

    /**
     * @param hazard      Hazard Input
     * @param units0      Units of the hazard input
     * @param t           Period, if applicable to the conversion
     * @param hazardType0 Type of hazard input
     * @param units1      Units of the required hazard
     * @param hazardType1 Type of required hazard
     * @return
     */
    private static double convertHazardType(double hazard, String units0, double t, String hazardType0, String units1, String hazardType1) {
        String concat = hazardType0.concat(hazardType1);
        if (concat.equalsIgnoreCase(sa_pgv)) {
            double pgv_from_sa = convertSAToPGV(hazard, units0, units1);
            return convertHazard(pgv_from_sa, units_cms, 0.0, PGV, units1, PGV);
        } else if (concat.equalsIgnoreCase(pga_pga)) {
            return getCorrectUnitsOfPGA(hazard, units0, units1);
        } else if (concat.equalsIgnoreCase(sa_sa)) {
            return getCorrectUnitsOfSA(hazard, units0, units1);
        } else if (concat.equalsIgnoreCase(sa_sd)) {
            return convertSAToSD(hazard, t, units0, units1);
        } else if (concat.equalsIgnoreCase(sa_sv)) {
            return convertSAToSV(hazard, t, units0);
        } else if (concat.equalsIgnoreCase(sd_sd)) {
            return getCorrectUnitsOfSD(hazard, units0, units1);
        } else if (concat.equalsIgnoreCase(pga_pgd)) {
            // logger.debug( "***************hazard val in: " + hazard );
            return convertPGAToPGD(hazard, units0, units1);
        } else if (concat.equalsIgnoreCase(pgv_pgv)) {
            // logger.debug( "***************hazard val in: " + hazard );
            return getCorrectUnitsOfPGV(hazard, units0, units1);
        }
        return hazard;
    }

    /**
     * @param sa_1   1.0 Second Spectral Acceleration
     * @param units0 Units of the SA value
     * @return PGV in cm/sec
     */
    private static double convertSAToPGV(double sa_1, String units0, String units1) {
        double hazard = sa_1;
        if (units_g.equalsIgnoreCase(units0)) {

        } else if (units_percg.equalsIgnoreCase(units0)) {
            hazard /= 100.0;
        }

        return ((386.4 * hazard) / (2 * Math.PI)) * 2.54 / 1.65;
    }

    /**
     * @param pga
     * @param units0
     * @param units1
     * @return
     */
    private static double convertPGAToPGD(double pga, String units0, String units1) {
        // XXX: assuming ground type B here, that's not always true, how do we
        // handle that?
        double hazard = pga;
        if (units_g.equalsIgnoreCase(units0)) {
            hazard *= 9.80;
        } else if (units_percg.equalsIgnoreCase(units0)) {
            hazard = hazard * 9.8 / 100.0;
        } else {
            logger.warn("unknown units in converting PGA to PGD, returning base hazard: " + hazard);
        }
        return getCorrectUnitsOfPGD(convertPGAToPGD(hazard, 1.2, 0.5, 2.0), "m", units1);
    }

    /**
     * @param pga Peak Ground Acceleration in m/s^2
     * @param s   Constant for specific ground type
     * @param t_c Constant for specific ground type
     * @param t_d Constant for specific ground type
     * @return Peak Ground Displacement in meters
     */
    private static double convertPGAToPGD(double pga, double s, double t_c, double t_d) {
        return (0.025 * pga * s * t_c * t_d);
    }

    /**
     * @param sa     spectral acceleration
     * @param t      period
     * @param units0 units of Sa
     * @return spectral displacement in cm
     */
    private static double convertSAToSD(double sa, double t, String units0, String units1) {
        sa = getCorrectUnitsOfSA(sa, units0, units_g);
        return getCorrectUnitsOfSD(sa * 9.78 * Math.pow(t, 2) * 2.54, units_cm, units1);
    }

    /**
     * @param sa     spectral acceleration
     * @param t      period
     * @param units0 units of Sa
     * @return spectral velocity in cm/s
     */
    private static double convertSAToSV(double sa, double t, String units0) {
        sa = getCorrectUnitsOfSA(sa, units0, units_g);
        return sa * 9.8 * t / (2 * Math.PI);
    }

    /**
     * @param pga
     * @param units0
     * @param units1
     * @return
     */
    public static double getCorrectUnitsOfPGA(double pga, String units0, String units1) {
        if (units1 != null && units1.equalsIgnoreCase(units0)) {
            return pga;
        } else if (units_g.equalsIgnoreCase(units1) && units_percg.equalsIgnoreCase(units0)) {
            return pga / 100.0;
        } else {
            logger.warn("Unknown PGA unit, returning unconverted pga value");
            // Unknown type
            return pga;
        }
    }

    /**
     * @param pgd
     * @param units0
     * @param units1
     * @return
     */
    public static double getCorrectUnitsOfPGD(double pgd, String units0, String units1) {
        if (units0 != null && units0.equalsIgnoreCase(units1)) {
            return pgd;
        } else if (units_m.equalsIgnoreCase(units0) || "m".equalsIgnoreCase(units0) && units_ft.equalsIgnoreCase(units1)) {
            return pgd * 3.2808399;
        } else if (units_m.equalsIgnoreCase(units0) || "m".equalsIgnoreCase(units0) && units_cm.equalsIgnoreCase(units1)) {
            return pgd * 100.0;
        } else {
            // Unknown type
            logger.warn("PGD unit type was " + units0 + ", but no conversion is implemented for units of " + units1);
            return pgd;
        }
    }

    /**
     * @param sa
     * @param units0
     * @param units1
     * @return
     */
    public static double getCorrectUnitsOfSA(double sa, String units0, String units1) {
        if (units1 != null && units1.equalsIgnoreCase(units0)) {
            return sa;
        } else if (units_g.equalsIgnoreCase(units1) && units_percg.equalsIgnoreCase(units0)) {
            return sa / 100.0;
        } else {
            // Unknown type
            logger.warn("Unknown SA unit, returning unconverted sa value");
            return sa;
        }
    }

    /**
     * @param sd
     * @param units0
     * @param units1
     * @return
     */
    public static double getCorrectUnitsOfSD(double sd, String units0, String units1) {
        if (units1 != null && units1.equalsIgnoreCase(units0)) {
            return sd;
        } else if (units_in.equalsIgnoreCase(units1) && units_cm.equalsIgnoreCase(units0)) {
            return sd / 2.54;
        } else if (units_cm.equalsIgnoreCase(units1) && units_in.equalsIgnoreCase(units0)) {
            return sd * 2.54;
        } else {
            // Unknown type
            logger.warn("Unknown SD unit, returning unconverted sd value");
            return sd;
        }
    }

    /**
     * @param pgv
     * @param units0
     * @param units1
     * @return
     */
    public static double getCorrectUnitsOfPGV(double pgv, String units0, String units1) {
        if (units1 != null && units1.equalsIgnoreCase(units0)) {
            return pgv;
        } else if (units_ins.equalsIgnoreCase(units1) && units_cms.equalsIgnoreCase(units0)) {
            return pgv / 2.54;
        } else {
            logger.warn("Unknown pgv unit, returning unconverted pgv value");
            return pgv;
        }
    }


    /**
     * @param period
     * @param hazard
     * @return
     */
    public static double convertMotionsToBC(String period, double hazard) {
        double convertedHazard = hazard;

        if (period == null) {
//            logger.warn("Null period, returning unconverted hazard value");
            return convertedHazard;
        }

        // Convert to B/C and the assume site class D
        if (period.equalsIgnoreCase(PGA)) {
            convertedHazard *= 1.52;
        } else if (period.equalsIgnoreCase("0.2")) {
            convertedHazard *= 1.76;
        } else if (period.equalsIgnoreCase("0.3")) {
            convertedHazard *= 1.72;
        } else if (period.equalsIgnoreCase("1.0") || period.equalsIgnoreCase("1")) {
            convertedHazard *= 1.34;
        }
        return convertedHazard;
    }

    /**
     * @param hazard
     * @return
     */
    public static String stripPeriod(String hazard) {
        hazard = hazard.replaceAll("[0-9]*", "");
        hazard = hazard.replaceAll("\\.*", "");
        hazard = hazard.replaceAll("sec", "");
        hazard = hazard.replaceAll(" ", "");
        if ("".equals(hazard)) {
            hazard = "Sa";
        }
        return hazard;
    }

    /**
     * @param hazard
     * @return
     */
    public static double getPeriod(String hazard) {
        hazard = hazard.replaceAll("[ a-zA-Z]*", "");
        try {
            return Double.parseDouble(hazard);
        } catch (NumberFormatException e) {
            return 0.0;
        }

    }

    /**
     * @param p1
     * @param p2
     * @return
     * @throws TransformException
     */
    public static double findDistance(Point p1, Point p2) throws TransformException {
        DefaultGeographicCRS crs = new DefaultGeographicCRS(DefaultGeographicCRS.WGS84);
        Coordinate c1 = new Coordinate(p1.getX(), p1.getY());
        Coordinate c2 = new Coordinate(p2.getX(), p2.getY());

        return JTS.orthodromicDistance(c1, c2, crs) / 1000.0;
    }

    /**
     * @param location
     * @param gc
     * @return
     * @throws PointOutsideCoverageException
     */
    public static double findRasterPoint(Point location, GridCoverage2D gc) throws PointOutsideCoverageException {
        double[] dest = null;
        final Point2D.Double point = new Point2D.Double();
        point.x = location.getX();
        point.y = location.getY();
        DirectPosition dp = new DirectPosition2D(location.getX(), location.getY());
        dest = gc.evaluate(dp, dest);
        if (Double.isNaN(dest[0])) {
            dest[0] = 0.0;
        }

        return dest[0];
    }

    public static String getDataServiceEndpoint() {
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = Config.getConfigProperties().getProperty("dataservice.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
    }

    public static String createDataset(String title, String creator, String description, String datasetType) throws IOException {
        String dataEndpoint = getDataServiceEndpoint();
        JSONArray spaces = new JSONArray();
        if (creator != null) {
            spaces.put(creator);
        }
        spaces.put(HazardConstants.ERGO_SPACE);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HazardConstants.DATA_TYPE, datasetType);
        jsonObject.put(HazardConstants.TITLE, title);
        jsonObject.put(HazardConstants.SOURCE_DATASET, "");
        jsonObject.put(HazardConstants.FORMAT, HazardConstants.RASTER_FORMAT);
        jsonObject.put(HazardConstants.DESCRIPTION, description);
        jsonObject.put(HazardConstants.SPACES, spaces);

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_CREDENTIAL_USERNAME, creator);

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.addTextBody(HazardConstants.DATASET_PARAMETER, jsonObject.toString());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        httpPost.setEntity(params.build());
        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JSONObject object = new JSONObject(responseStr);

            String datasetId = object.getString("id");
            return datasetId;
        }

        return null;
    }

    public static String createRasterDataset(File rasterFile, String title, String creator, String description, String datasetType) throws IOException {
        String datasetId = createDataset(title, creator, description, datasetType);

        if (datasetId != null) {
            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            params.addBinaryBody(HazardConstants.FILE_PARAMETER_, rasterFile);

            attachFileToDataset(datasetId, creator, params);
        }

        return datasetId;
    }

    public static String createRasterDataset(String filename, InputStream fis, String title, String creator, String description, String datasetType) throws IOException {
        String datasetId = createDataset(title, creator, description, datasetType);
        if (datasetId != null) {
            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            params.addBinaryBody(HazardConstants.FILE_PARAMETER_, fis, ContentType.DEFAULT_BINARY, filename);

            attachFileToDataset(datasetId, creator, params);
        }
        return datasetId;
    }

    public static void attachFileToDataset(String datasetId, String creator, MultipartEntityBuilder params) throws IOException {
        String dataEndpoint = getDataServiceEndpoint();
        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        requestUrl += "/" + datasetId + "/" + HazardConstants.DATASETS_FILES;

        // Attach file
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_CREDENTIAL_USERNAME, creator);
        httpPost.setEntity(params.build());

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        HttpResponse response = httpclient.execute(httpPost);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = responseHandler.handleResponse(response);

        // This could be useful if there is a failure
        logger.debug("Attach file response " + responseStr);
    }

    public static File getFileFromDataService(String datasetId, String creator, File incoreWorkDirectory) {
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = Config.getConfigProperties().getProperty("dataservice.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        InputStream inputStream = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpclient = builder.build();

            String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT + "/" + datasetId + "/blob";
            HttpGet httpGet = new HttpGet(requestUrl);
            httpGet.setHeader(HazardConstants.X_CREDENTIAL_USERNAME, creator);

            HttpResponse response = null;

            response = httpclient.execute(httpGet);
            inputStream = response.getEntity().getContent();
        } catch (IOException e) {
            // TODO add logging
            logger.error(e);
        }

        String filename = "files.zip";
        File file = new File(incoreWorkDirectory, filename);

        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))
        ) {

            int inByte;
            while ((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }

        } catch (IOException e) {

            logger.error(e);
        }

        return file;
    }

    public static GridCoverage getGridCoverage(String datasetId, String creator) {
        File incoreWorkDirectory = getWorkDirectory();
        File file = getFileFromDataService(datasetId, creator, incoreWorkDirectory);

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

    public static FeatureCollection getFeatureCollection(String datasetId, String creator) {

        File incoreWorkDirectory = getWorkDirectory();
        File file = getFileFromDataService(datasetId, creator, incoreWorkDirectory);

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
                if (fileExt.equalsIgnoreCase("shp")) {
                    inSourceFileUrl = newFile.toURI().toURL();
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            logger.error("Error unzipping shapefile", e);
            return null;
        }

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

    public static File getWorkDirectory() {
        File incoreWorkDirectory = null;
        try {
            incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            return incoreWorkDirectory;
        } catch (IOException e) {
            logger.error("Error creating temporary directory.", e);
        }
        return incoreWorkDirectory;
    }


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

    /**
     * @param hazardType0
     * @param period
     * @return
     */
    public static String[] findHazardConversionTypes(String hazardType0, double period) {
        if (PGA.equalsIgnoreCase(hazardType0)) {
            String[] s = {PGA, PGD};
            return s;
        } else if (SA.equalsIgnoreCase(hazardType0)) {
            if (Double.compare(period, 1.0) == 0) {
                String[] s = {"Sa", "Sd", "Sv", PGV}; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                return s;
            } else {
                String[] s = {"Sa", "Sd", "Sv"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return s;
            }
        } else {
            String[] s = {hazardType0};
            return s;
        }
    }

    public static HazardDataset findHazard(List<HazardDataset> hazardDataset, String demandHazard, String period, boolean exactOnly) {
        return findHazard(hazardDataset, demandHazard, Double.parseDouble(period), exactOnly);

    }

    public static HazardDataset findHazard(List<HazardDataset> hazardDataset, String demandHazard, double period, boolean exactOnly) {
        String demandHazardMotion = stripPeriod(demandHazard);
        List<HazardDataset> matches = new LinkedList<>();
        for (HazardDataset dataset : hazardDataset) {
            if (dataset.getDemandType().equalsIgnoreCase(demandHazardMotion)) {
                matches.add(dataset);
                // return r;
            }
        }

        if (matches.size() == 0) {
            // Second Attempt, now we look for conversion possibilities
            for (HazardDataset dataset : hazardDataset) {

                double rasterPeriod = dataset.getPeriod();

                String[] conversions = HazardUtil.findHazardConversionTypes(dataset.getDemandType(), rasterPeriod);
                for (String s : conversions) {
                    if (s.equalsIgnoreCase(demandHazardMotion)) {
                        if (!matches.contains(dataset)) {
                            matches.add(dataset);
                        }
                    }
                }
            }
        }

        // if we're looking for exact period matches only, don't bother with
        // that stuff later that looks for the closest period...

        if (exactOnly) {
            for (HazardDataset rasterDataset : matches) {
                double rasterPeriod = rasterDataset.getPeriod(); //$NON-NLS-1$
                if (Math.abs(rasterPeriod - period) < .001) {
                    return rasterDataset;
                }
            }
            return null;
        }

        if (matches.size() == 0) {
            logger.debug("Did not find appropriate hazard or a conversion"); //$NON-NLS-1$
            logger.debug("Fragility curve requires hazard type: " + demandHazard); //$NON-NLS-1$
            logger.debug("Here are the hazard types we have: "); //$NON-NLS-1$
            for (HazardDataset dataset : hazardDataset) {
                logger.debug(dataset.getDemandType());
            }
            return null;
        } else if (matches.size() == 1) {
            return matches.get(0);
        } else {
            HazardDataset returnVal = matches.get(0);
            double period_diff = Math.abs(period - returnVal.getPeriod());
            for (int i = 1; i < matches.size(); i++) {
                double tmp = Math.abs(period - matches.get(i).getPeriod());
                if (tmp < period_diff) {
                    period_diff = tmp;
                    returnVal = matches.get(i);
                }
            }

            return returnVal;
        }

    }

    public static String[] getHazardDemandComponents(String fullDemandType) {
        String[] demandParts = {"0.0", fullDemandType};
        if (fullDemandType.equalsIgnoreCase(PGA) || fullDemandType.equalsIgnoreCase(PGV) || fullDemandType.equalsIgnoreCase(PGD)) {
            return demandParts;
        } else {
            String[] demandSplit = fullDemandType.split(" ");
            demandParts[0] = demandSplit[0].trim();
            demandParts[1] = demandSplit[1].trim();

            return demandParts;
        }
    }

    public static String getFullDemandType(String period, String demandType) {
        return getFullDemandType(Double.parseDouble(period), demandType);
    }

    public static String getFullDemandType(double period, String demandType) {
        if (demandType.equalsIgnoreCase(PGA) || demandType.equalsIgnoreCase(PGV) || demandType.equalsIgnoreCase(PGD)) {
            return demandType;
        } else {
            return Double.toString(period) + " " + demandType.trim();
        }
    }


}
