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

package edu.illinois.ncsa.incore.service.data.geotools;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.data.controllers.DatasetController;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by ywkim on 10/5/2017.
 */
public class GeotoolsUtils {
    private static final String UNI_ID_SHP = "guid";
    private static final String UNI_ID_CSV = "guid";
    private static final String DATA_FIELD_GEOM = "the_geom";
    private static final Logger logger = Logger.getLogger(GeotoolsUtils.class);
//    public static String outFileName = null;

    /**
     * convert ascii grid to geotiff
     *
     * @param inAsc
     * @return
     * @throws IOException
     */
    public static File convertAscToGeotiff(File inAsc) throws IOException {
        String tmpName = inAsc.getName();
        String outTifName = FileUtils.changeFileNameExtension(tmpName, "tif");
        File outTif = new File(outTifName);
        ArcGridReader ascGrid = new ArcGridReader(inAsc);
        GridCoverage2D gc = (GridCoverage2D) ascGrid.read(null);

        try {
            GeoTiffWriteParams wp = new GeoTiffWriteParams();
            wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
            wp.setCompressionType("LZW");
            ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
            GeoTiffWriter writer = new GeoTiffWriter(outTif);
            writer.write(gc, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
        } catch (Exception e) {
            logger.error("failed to conver ascii file to geotiff.");
            throw new IOException("failed to convert ascii file to geotiff ", e);
        }

        return outTif;
    }

    /**
     * join csv file to shapefile and create a new shapefile
     *
     * @param shpfiles
     * @param csvFile
     * @return
     * @throws IOException
     */
    public static File joinTableShapefile(Dataset dataset, List<File> shpfiles, File csvFile, boolean isRename) throws IOException {
        // set geometry factory
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        String outFileName = FilenameUtils.getBaseName(csvFile.getName()) + "." + FileUtils.EXTENSION_SHP;

        // read csv file
        String[] csvHeaders = getCsvHeader(csvFile);
        List<String[]> csvRows = readCsvFile(csvFile);
        int csvIdLoc = 0;

        // remove quotes in header
        for (int i = 0; i < csvHeaders.length; i++) {
            csvHeaders[i] = csvHeaders[i].replaceAll("^\"|\"$", "");
        }

        // find column location of the unique id inCsv
        for (int i = 0; i < csvHeaders.length - 1; i++) {
            String header = csvHeaders[i];
            if (header.equals(UNI_ID_CSV)) {
                csvIdLoc = i;
            }
        }

        // create temp dir and copy files to temp dir
        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
        List<File> copiedFileList = null;
        if (isRename) { // this will only get the files for geoserver
            outFileName = dataset.getId() + "." + FileUtils.EXTENSION_SHP;
            copiedFileList = copyFilesToTmpDir(shpfiles, tempDir, dataset.getId(), true, "shp");
        } else {
            copiedFileList = copyFilesToTmpDir(shpfiles, tempDir, "", false, "");
        }
        URL inSourceFileUrl = null;
        for (File copiedFile : copiedFileList) {
            String fileExt = FilenameUtils.getExtension(copiedFile.getName());
            if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                inSourceFileUrl = copiedFile.toURI().toURL();
            }
        }

        DataStore store = getShapefileDataStore(inSourceFileUrl, false);
        FileDataStore fileStore = (FileDataStore) store;
        SimpleFeatureSource featureSource = fileStore.getFeatureSource();
        SimpleFeatureCollection inputFeatures = featureSource.getFeatures();

        SimpleFeatureIterator inputFeatureIterator = inputFeatures.features();
        List<Geometry> tmpList = new LinkedList<Geometry>();
        List<Geometry> finalList = new LinkedList<Geometry>();
        List<Map> resultMapList = new LinkedList<Map>();
        List<Map> finalResultMapList = new LinkedList<Map>();

        try {
            while (inputFeatureIterator.hasNext()) {
                SimpleFeature inputFeature = inputFeatureIterator.next();
                Geometry g = (Geometry) inputFeature.getAttribute(0);
                Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

                tmpList.clear();
                resultMapList.clear();

                for (int i = 0; i < inputFeature.getAttributeCount(); i++) {
                    AttributeDescriptor attributeType = inputFeature.getFeatureType().getDescriptor(i);
                    Object attribute = inputFeature.getAttribute(i);
                    String csvConnector = null;

                    // resultMap is an actual table.
                    if (attributeType.getLocalName() != DATA_FIELD_GEOM) {
                        resultMap.put(attributeType.getLocalName(), attribute);
                        // check if this is unique id
                        if (attributeType.getLocalName().equalsIgnoreCase(UNI_ID_SHP)) {
                            if (attribute instanceof Integer || attribute instanceof Float
                                    || attribute instanceof Double) {
                                csvConnector = String.valueOf(attribute);
                            } else {
                                csvConnector = (String) attribute;
                            }

                            // find matching csv rows
                            String[] matchedCsvRow = null;
                            for (int j = 0; j < csvRows.size(); j++) {
                                if (csvRows.get(j)[csvIdLoc].equals(csvConnector)) {
                                    matchedCsvRow = csvRows.get(j);
                                    csvRows.remove(j);
                                }
                            }

                            // add matched row to resultMap
                            if (matchedCsvRow != null) {
                                for (int j = 0; j < matchedCsvRow.length; j++) {
                                    if (j != csvIdLoc) {
                                        resultMap.put(csvHeaders[j], matchedCsvRow[j]);

                                    }
                                }
                            }
                        }

                    }
                }
                resultMapList.add(resultMap);

                finalList.add(g);
                finalResultMapList.addAll(resultMapList);
            }
        } finally {
            inputFeatureIterator.close();
        }

        return createOutfile(finalList, finalResultMapList, tempDir, outFileName);
    }

    /**
     * copy files in the list to the temporary directory
     *
     * @param fileList
     * @param tempDir
     * @param datasetId
     * @param isGeoserver
     * @param inExt
     * @return
     * @throws IOException
     */
    public static List<File> copyFilesToTmpDir(List<File> fileList, String tempDir, String datasetId, boolean isGeoserver, String inExt) throws IOException {
        List<File> outList = new ArrayList<File>();
        for (int i = 0; i < fileList.size(); i++) {
            File sourceFile = fileList.get(i);
            String fileName = FilenameUtils.getName(sourceFile.getName());
            String fileExt = "";
            if (isGeoserver) {
                fileExt = FilenameUtils.getExtension(sourceFile.getName());
                File newFile = new File(sourceFile.getParent() + File.separator + datasetId + "." + fileExt);
                fileName = FilenameUtils.getName(newFile.getName());
            }
            File destFile = new File(tempDir + File.separator + fileName);
            if (isGeoserver) {
                if (inExt.equalsIgnoreCase("shp")) {
                    if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("dbf") ||
                            fileExt.equalsIgnoreCase("shx") || fileExt.equalsIgnoreCase("prj")) {
                        outList.add(destFile);
                    }
                } else if (inExt.equalsIgnoreCase("tif")) {
                    if (fileExt.equalsIgnoreCase(inExt)) {
                        outList.add(destFile);
                    }
                } else if (inExt.equalsIgnoreCase("asc")) {
                    if (fileExt.equalsIgnoreCase(inExt)) {
                        outList.add(destFile);
                    }
                }
            } else {
                outList.add(destFile);
            }

            org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
            //in here I am simply copy and paste the files to temp direcotry.
            // However, if the source file is in different server, use httpdownloader
            // HttpDownloader.downloadFile(inSourceFileUrlTwo.getFile(), tempDir);
        }

        return outList;
    }

    /**
     * create an output shapefile
     *
     * @param finalList
     * @param resultMapList
     * @param outDir
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static File createOutfile(List<Geometry> finalList, @SuppressWarnings("rawtypes") List<Map> resultMapList,
                                     String outDir, String outFileName) throws IOException {
        File outFile = new File(outDir + File.separator + outFileName);

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

        AttributeTypeBuilder attBuilder = new AttributeTypeBuilder();
        attBuilder.setName("the_geom");
        attBuilder.setBinding(finalList.get(0).getClass());
        attBuilder.crs(DefaultGeographicCRS.WGS84);

        GeometryType geomType = attBuilder.buildGeometryType();
        GeometryDescriptor geomDesc = attBuilder.buildDescriptor("the_geom", geomType);

        builder.setName("output");
        builder.add(geomDesc);
//		builder.add("NEW_UNI_ID", Integer.class);   	// to add new unique id column

        // create the columns
        Map<String, Object> columnMap = resultMapList.get(0);
        for (String name : columnMap.keySet()) {
            builder.add(name, String.class);
        }


        SimpleFeatureType schema = builder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        for (int i = 0; i < finalList.size(); i++) {
            columnMap = resultMapList.get(i);
            featureBuilder.add(finalList.get(i));
//			featureBuilder.add(i);	// this line is for new unique id field
            for (String name : columnMap.keySet()) {
                Object value = resultMapList.get(i).get(name);
                featureBuilder.add(value);
            }
            SimpleFeature feature = featureBuilder.buildFeature(null);
            collection.add(feature);
        }
        return outToFile(outFile, schema, collection);
    }

    /**
     * create actual output shapefile in the directory
     *
     * @param pathFile
     * @param schema
     * @param collection
     * @return
     * @throws IOException
     */
    public static File outToFile(File pathFile, SimpleFeatureType schema, DefaultFeatureCollection collection)
            throws IOException {
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
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);
        }

        return prepareShpZipFile(pathFile);
    }

    /**
     * create a zip file of the shapefile related ones
     *
     * @param shpFile
     * @return
     * @throws IOException
     */
    public static File prepareShpZipFile(File shpFile) throws IOException {
        String absolutePath = shpFile.getAbsolutePath();
        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
        String shpBaseName = FilenameUtils.getBaseName(shpFile.getName());
        List<String> zipFileList = new LinkedList<String>();
        File[] filesInDir = new File(filePath).listFiles();
        for (File file : filesInDir) {
            String fileBaseName = FilenameUtils.getBaseName(file.getName());
            if (fileBaseName.equalsIgnoreCase(shpBaseName)) {
                zipFileList.add(file.getName());
            }
        }

        FileUtils.createZipFile(zipFileList, filePath, shpBaseName);

        return new File(filePath + File.separator + shpBaseName + ".zip");
    }

    /**
     * get shapefile data store for creating new shapefile
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

    /**
     * read csv file
     *
     * @param inCsv
     * @return
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static List<String[]> readCsvFile(File inCsv) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(inCsv), ',', '"', 1);
        List<String[]> rows = reader.readAll();
        return rows;
    }

    /**
     * obtain csv header information
     *
     * @param inCsv
     * @return
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static String[] getCsvHeader(File inCsv) throws IOException {
        String splitter = ",";
        BufferedReader reader = new BufferedReader(new FileReader(inCsv));
        String firstLine = reader.readLine();
        String[] header = null;
        if (firstLine != null) {
            header = firstLine.split(splitter);
        }

        return header;
    }

    /**
     * create GUID field if there is none
     *
     * @param dataset
     * @param shpfiles
     * @return
     * @throws IOException
     */
    public static boolean createGUIDinShpfile(Dataset dataset, List<File> shpfiles) throws IOException {
        // set geometry factory
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        String outFileName = FilenameUtils.getBaseName(shpfiles.get(0).getName()) + "." + FileUtils.EXTENSION_SHP;

        // create temp dir and copy files to temp dir
        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
        List<File> copiedFileList = copyFilesToTmpDir(shpfiles, tempDir, "", false, "shp");
        URL inSourceFileUrl = null;
        boolean isGuid = false;

        for (File copiedFile : copiedFileList) {
            String fileExt = FilenameUtils.getExtension(copiedFile.getName());
            if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                inSourceFileUrl = copiedFile.toURI().toURL();
            }
        }

        DataStore store = getShapefileDataStore(inSourceFileUrl, false);
        FileDataStore fileStore = (FileDataStore) store;
        SimpleFeatureSource featureSource = fileStore.getFeatureSource();
        SimpleFeatureCollection inputFeatures = featureSource.getFeatures();

        List<Geometry> tmpList = new LinkedList<Geometry>();
        List<Geometry> finalList = new LinkedList<Geometry>();
        List<Map> resultMapList = new LinkedList<Map>();
        List<Map> finalResultMapList = new LinkedList<Map>();

        // check if there is guid exists
        isGuid = isGuidExist(inputFeatures);

        // creaste new dbf in the temp dir and move it to data repo directory
        if (isGuid != true) {
            SimpleFeatureIterator inputFeatureIterator = inputFeatures.features();
            try {
                while (inputFeatureIterator.hasNext()) {
                    SimpleFeature inputFeature = inputFeatureIterator.next();
                    Geometry g = (Geometry) inputFeature.getAttribute(0);
                    Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

                    tmpList.clear();
                    resultMapList.clear();

                    for (int i = 0; i < inputFeature.getAttributeCount(); i++) {
                        AttributeDescriptor attributeType = inputFeature.getFeatureType().getDescriptor(i);
                        Object attribute = inputFeature.getAttribute(i);

                        // resultMap is an actual table.
                        if (attributeType.getLocalName() != DATA_FIELD_GEOM) {
                            resultMap.put(attributeType.getLocalName(), attribute);
                        }

                        // create GUID
                        if (!isGuid) {
                            UUID uuid = UUID.randomUUID();
                            resultMap.put(UNI_ID_SHP, uuid.toString());
                        }
                    }
                    resultMapList.add(resultMap);
                    finalList.add(g);
                    finalResultMapList.addAll(resultMapList);
                }
                File outFile = createOutfile(finalList, finalResultMapList, tempDir, outFileName);
                FileUtils.switchDbfFile(outFile, shpfiles);
            } finally {
                inputFeatureIterator.close();
            }
        }

        FileUtils.deleteTmpDir(copiedFileList.get(0));

        if (isGuid) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check if the guid exists
     * @param inputFeatures
     * @return
     */
    public static boolean isGuidExist(SimpleFeatureCollection inputFeatures) {
        boolean isGuid = false;
        SimpleFeatureIterator inputFeatureIterator = inputFeatures.features();
        try {
            while (inputFeatureIterator.hasNext()) {
                SimpleFeature inputFeature = inputFeatureIterator.next();
                for (int i = 0; i < inputFeature.getAttributeCount(); i++) {
                    AttributeDescriptor attributeType = inputFeature.getFeatureType().getDescriptor(i);
                    if (attributeType.getLocalName().equalsIgnoreCase(UNI_ID_SHP)) {
                        isGuid = true;
                    }
                }
            }
        } finally {
            inputFeatureIterator.close();
        }

        return isGuid;
    }
}

