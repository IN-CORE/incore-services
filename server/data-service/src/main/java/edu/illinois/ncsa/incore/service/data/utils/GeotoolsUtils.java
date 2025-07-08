/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.GeoUtils;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.api.geometry.Bounds;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.*;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.data.FeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.filter.Filter;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.parameter.ParameterValueGroup;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.api.data.*;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.locationtech.jts.geom.Point;
import org.geotools.data.DefaultTransaction;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

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
        GridCoverage2D gc = ascGrid.read(null);

        try {
            GeoTiffWriteParams wp = new GeoTiffWriteParams();
            wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
            wp.setCompressionType("LZW");
            ParameterValueGroup params = new GeoTiffFormat().getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
            GeoTiffWriter writer = new GeoTiffWriter(outTif);
            writer.write(gc, params.values().toArray(new GeneralParameterValue[1]));
        } catch (Exception e) {
            logger.error("failed to conver ascii file to geotiff.");
            throw new IOException("failed to convert ascii file to geotiff ", e);
        }

        return outTif;
    }

    /**
     * get bounding box information from tif
     *
     * @param file
     * @return
     */
    public static double[] getBboxFromGrid(File file) throws IOException {
        double[] bbox = new double[4];
        // create temp dir and copy files to temp dir
//        List<File> copiedFileList = copyFilesToTempDir(file);
//        File file = copiedFileList.get(0);
        GridCoverage2D coverage = getGridCoverage(file);
        Bounds env = coverage.getEnvelope();

        bbox[0] = env.getLowerCorner().getCoordinate()[0];
        bbox[1] = env.getLowerCorner().getCoordinate()[1];
        bbox[2] = env.getUpperCorner().getCoordinate()[0];
        bbox[3] = env.getUpperCorner().getCoordinate()[1];

        return bbox;
    }

    /**
     * create temp dir and copy files to temp dir
     *
     * @param files
     * @return
     * @throws IOException
     */
    public static List<File> copyFilesToTempDir(List<File> files) throws IOException {
        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
        List<File> copiedFileList = null;
        copiedFileList = performCopyFiles(files, tempDir, "", false, "");

        return copiedFileList;
    }

    public static GridCoverage2D getGridCoverage(File file) throws IOException {
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        GridCoverage2DReader reader = format.getReader(file);
        GridCoverage2D coverage = reader.read(null);
        reader.dispose();

        return coverage;
    }

    /**
     * get bounding box information from shapefile
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static double[] getBboxFromShp(File file) throws IOException {
        // create temp dir and copy files to temp dir
        double[] bbox = new double[4];
//        List<File> copiedFileList = copyFilesToTempDir(shpfiles);

        SimpleFeatureCollection sfc = getSimpleFeatureCollectionFromFile(file);
        ReferencedEnvelope env = sfc.getBounds();
        double minx = env.getMinX();
        double miny = env.getMinY();
        double maxx = env.getMaxX();
        double maxy = env.getMaxY();

        bbox[0] = minx;
        bbox[1] = miny;
        bbox[2] = maxx;
        bbox[3] = maxy;

        return bbox;
    }

    public static double[] getBboxFromGeopackage(SimpleFeatureCollection sfc) throws IOException {
        double[] bbox = new double[4];

        ReferencedEnvelope env = sfc.getBounds();
        double minx = env.getMinX();
        double miny = env.getMinY();
        double maxx = env.getMaxX();
        double maxy = env.getMaxY();

        bbox[0] = minx;
        bbox[1] = miny;
        bbox[2] = maxx;
        bbox[3] = maxy;

        return bbox;
    }

    /**
     * get SimpleFeatureCollection from list of shapefile components
     *
     * @param fileList
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection getSimpleFeatureCollectionFromFileList(List<File> fileList) throws IOException {
        File inSourceFile = null;
        for (File copiedFile : fileList) {
            String fileExt = FilenameUtils.getExtension(copiedFile.getName());
            if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                inSourceFile = copiedFile;
            }
        }
        SimpleFeatureCollection sfc = getSimpleFeatureCollectionFromFile(inSourceFile);

        return sfc;
    }

    /**
     * create SimpleFeatureCollection from file (shapefile)
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection getSimpleFeatureCollectionFromFile(File file) throws IOException {
        URL inSourceFileUrl = file.toURI().toURL();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", inSourceFileUrl);

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);

        Filter filter = Filter.INCLUDE;
        dataStore.dispose();
        SimpleFeatureCollection sfc = (SimpleFeatureCollection) source.getFeatures(filter);

        return sfc;
    }

    /**
     * create SimpleFeatureCollection from geopackage file
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static SimpleFeatureCollection getSimpleFeatureCollectionFromGeopackage(File file) throws IOException {
        DataStore dataStore = null;
        HashMap<String, Object> map = new HashMap<>();
        map.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
        map.put(GeoPkgDataStoreFactory.DATABASE.key, file.getAbsoluteFile());
        dataStore = DataStoreFinder.getDataStore(map);
        if (dataStore == null) {
            throw new IOException("Unable to open geopackage file");
        }

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureCollection sfc = dataStore.getFeatureSource(typeName).getFeatures();

        return sfc;
    }

    /**
     * join csv file to shapefile and create a new geopackage file
     *
     * @param shpfiles
     * @param csvFile
     * @return
     * @throws IOException
     */
    public static File joinTableShapefile(Dataset dataset, List<File> shpfiles, File csvFile, boolean isRename) throws IOException, CsvValidationException {
        String outFileName = FilenameUtils.getBaseName(csvFile.getName()) + "." + FileUtils.EXTENSION_SHP;

        // create temp dir and copy files to temp dir
        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
        String shapeFileName = FilenameUtils.getBaseName(shpfiles.get(0).getName());
        List<File> copiedFileList = null;
        if (isRename) { // this will only get the files for geoserver
            outFileName = dataset.getId() + "." + FileUtils.EXTENSION_SHP;
            shapeFileName = dataset.getId();
            copiedFileList = performCopyFiles(shpfiles, tempDir, dataset.getId(), true, "shp");
        } else {
            copiedFileList = performCopyFiles(shpfiles, tempDir, "", false, "");
        }

        // create shapefile source by reading shapefile from copied file list
        SimpleFeatureSource shapefileSource = createShapefileSource(String.valueOf(copiedFileList.get(0)));

        // read csv file
        SimpleFeatureType csvFeatureType = createCsvFeatureType(csvFile.getPath(), shapeFileName);
        SimpleFeatureCollection csvFeatures = createCsvFeatureFromCsvType(csvFile.getPath(), csvFeatureType);

        DefaultFeatureCollection newCollection = (DefaultFeatureCollection) performInnerJoin(csvFeatures, shapefileSource, UNI_ID_CSV, dataset.getId());

        // to make an output to shapefile, use this
//        File outShapefile = outToShapefile(joinedFeatures, tempDir, outFileName, shapefileSource);
//        return outShapefile;

        // to make an output to file, use this
        // return outToFile(new File(tempDir + File.separator + outFileName), newSft, newCollection);

        // to make an output to geopackage, use this
        return outToGpkgFile(new File(tempDir + File.separator + dataset.getId() + "." + FileUtils.EXTENSION_GEOPACKAGE), newCollection);
    }

    public static File outToShapefile(SimpleFeatureCollection features, String outputDir, String outputFileName,
                                      SimpleFeatureSource inputShapefileSource) throws IOException {
        File shapefileOutputFile = new File(outputDir, outputFileName);
        Map<String, Serializable> shapefileParams = Map.of(
            "url", shapefileOutputFile.toURI().toURL(),
            "create spatial index", Boolean.TRUE
        );

        try {
            // create ShapefileDataStore
            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
            ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(shapefileParams);

            // set feature type
            shapefileDataStore.createSchema(features.getSchema());

            // set the same projection as the input shapefile
            shapefileDataStore.forceSchemaCRS(inputShapefileSource.getSchema().getCoordinateReferenceSystem());

            // write features to Shapefile
            Transaction transaction = new DefaultTransaction("create");
            String typeName = shapefileDataStore.getTypeNames()[0];
            FeatureStore<SimpleFeatureType, SimpleFeature> featureStore =
                (FeatureStore<SimpleFeatureType, SimpleFeature>) shapefileDataStore.getFeatureSource(typeName);
            featureStore.addFeatures(features);
            transaction.commit();
            transaction.close();

        } catch (Exception e) {
            throw new IOException("Error writing features to Shapefile", e);
        }
        return shapefileOutputFile;
    }

    public static SimpleFeatureSource createShapefileSource(String shapefilePath) throws IOException {
        // convert extension to shp if it is not shp
        String ext = FilenameUtils.getExtension(shapefilePath);
        if (!ext.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
            shapefilePath = FileUtils.changeFileNameExtension(shapefilePath, FileUtils.EXTENSION_SHP);
        }

        // create SimpleFeatureSource from shapefile
        Map<String, Serializable> shapefileParams = Map.of("url", new File(shapefilePath).toURI().toURL());

        DataStore shapefileStore = DataStoreFinder.getDataStore(shapefileParams);
        if (shapefileStore == null) {
            throw new IOException("Unable to create DataStore for shapefile. Check if the file is valid.");
        }

        String typeName = shapefileStore.getTypeNames()[0];
        SimpleFeatureSource sfs = shapefileStore.getFeatureSource(typeName);

        return sfs;
    }

    public static SimpleFeatureCollection createCsvFeatureFromCsvType(String csvFilePath, SimpleFeatureType featureType)
        throws IOException, CsvValidationException {
        // create SimpleFeatureCollection from csv file
        List<SimpleFeature> csvFeatures = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] header = reader.readNext(); // Assuming the first row is header

            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

            String[] line;
            while ((line = reader.readNext()) != null) {
                for (int i = 0; i < header.length; i++) {
                    featureBuilder.set(header[i], convertStringToType(line[i], featureType.getDescriptor(i).getType().getBinding()));
                }

                SimpleFeature feature = featureBuilder.buildFeature(null);
                csvFeatures.add(feature);
            }
        }

        SimpleFeatureCollection collection = DataUtilities.collection(csvFeatures);

        return collection;
    }

    public static Object convertStringToType(String value, Class<?> outType) {
        // this is to add the correct type to the feature,
        // currently it only supports integer, double, and string
        if (outType.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (outType.equals(Double.class)) {
            return Double.parseDouble(value);
        } else {
            // if it is not interger or double, it is string,
            // if not, there should be some additional logic should be added
            return value;
        }
    }

    public static SimpleFeatureType createCsvFeatureType(String csvFilePath, String sourceName) throws IOException, CsvValidationException {
        // read CSV file to get column names and types
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] header = reader.readNext(); // assuming the first row is the header

            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("CsvFeatureType_" + sourceName);

            for (String columnName : header) {
                // assume all columns are of type String to make it simple
                builder.add(columnName, String.class);
            }

            SimpleFeatureType builtFeatureType = builder.buildFeatureType();

            return builtFeatureType;
        }
    }

    public static SimpleFeatureCollection performInnerJoin(SimpleFeatureCollection csvFeatures, SimpleFeatureSource shapefileSource,
                                                           String commonFieldName, String datasetId) throws IOException {

        // create an index for the CSV features
        Map<Object, SimpleFeature> csvFeatureIndex = indexFeatures(csvFeatures, commonFieldName);

        // create joined feature type
        SimpleFeatureType joinedFeatureType = createJoinedFeatureType(csvFeatures.getSchema(), shapefileSource.getSchema(), datasetId);

        // create a DefaultFeatureCollection to store the joined features
        DefaultFeatureCollection joinedFeatures = new DefaultFeatureCollection(null, joinedFeatureType);

        try (SimpleFeatureIterator shapefileIterator = shapefileSource.getFeatures().features()) {
            while (shapefileIterator.hasNext()) {
                SimpleFeature shapefileFeature = shapefileIterator.next();
                Object commonFieldValue = shapefileFeature.getAttribute(commonFieldName);

                if (csvFeatureIndex.containsKey(commonFieldValue)) {
                    SimpleFeature csvFeature = csvFeatureIndex.get(commonFieldValue);
                    SimpleFeature joinedFeature = createJoinedFeature(csvFeature, shapefileFeature, joinedFeatureType);
                    joinedFeatures.add(joinedFeature);
                }
            }
        }

        return joinedFeatures;
    }

    public static Map<Object, SimpleFeature> indexFeatures(SimpleFeatureCollection featureCollection, String commonFieldName) {
        Map<Object, SimpleFeature> featureIndex = new HashMap<>();

        try (SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Object commonFieldValue = feature.getAttribute(commonFieldName);
                featureIndex.put(commonFieldValue, feature);
            }
        }

        return featureIndex;
    }

    public static SimpleFeature createJoinedFeature(SimpleFeature csvFeature, SimpleFeature shapefileFeature,
                                                    SimpleFeatureType joinedFeatureType) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(joinedFeatureType);

        List<String> attributes = new LinkedList<String>();
        // copy all attributes from shapefile feature
        for (int i = 0; i < shapefileFeature.getAttributeCount(); i++) {
            String localName = shapefileFeature.getFeatureType().getDescriptor(i).getLocalName();
            attributes.add(localName);
            builder.add(shapefileFeature.getAttribute(i));
        }

        // copy all attributes from CSV feature that are not in the original shapefile
        for (int i = 0; i < csvFeature.getAttributeCount(); i++) {
            String localName = csvFeature.getFeatureType().getDescriptor(i).getLocalName();
            if (!attributes.contains(localName)) {
                builder.add(csvFeature.getAttribute(i));
            }
        }

        SimpleFeature joinedFeature = builder.buildFeature(null);

        return joinedFeature;
    }

    public static SimpleFeatureType createJoinedFeatureType(SimpleFeatureType csvType, SimpleFeatureType shapefileType, String datasetId) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init(shapefileType);
        if (!datasetId.equals("")) {
            builder.setName(datasetId);
        } else {
            builder.setName("JoinedFeatureType");
        }

        addAttributesWithoutDuplicate(builder, csvType, "guid");
//        addAttributesWithoutDuplicate(builder, shapefileType, "guid");
//        GeometryDescriptor geometryDescriptor = shapefileType.getGeometryDescriptor();
//        if (geometryDescriptor != null) {
//            builder.setCRS(geometryDescriptor.getCoordinateReferenceSystem());
//            builder.add("geometry", geometryDescriptor.getType().getBinding());
//        }

        SimpleFeatureType builtFeatureType = builder.buildFeatureType();

        return builtFeatureType;
    }

    public static void addAttributesWithoutDuplicate(SimpleFeatureTypeBuilder builder, SimpleFeatureType sourceType) {
        addAttributesWithoutDuplicate(builder, sourceType, null);
    }

    public static void addAttributesWithoutDuplicate(SimpleFeatureTypeBuilder builder, SimpleFeatureType sourceType,
                                                     String excludeAttribute) {
        Set<String> addedAttributes = new HashSet<>();

        for (int i = 0; i < sourceType.getAttributeCount(); i++) {
            String attributeName = sourceType.getDescriptor(i).getLocalName();

            // Exclude the specified attribute
            if (excludeAttribute == null || !attributeName.equals(excludeAttribute)) {
                builder.add(attributeName, sourceType.getDescriptor(i).getType().getBinding());
                addedAttributes.add(attributeName);
            }
        }
    }

    public static File joinTableGeopackage(Dataset dataset, String gpkgFileName, File csvFile, boolean isRename) throws IOException, CsvException {
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
        File gpkgfile = new File(gpkgFileName);
        List<File> gpkfiles = null;
        gpkfiles.add(gpkgfile);
        List<File> copiedFileList = null;
        if (isRename) { // this will only get the files for geoserver
            outFileName = dataset.getId() + "." + FileUtils.EXTENSION_GPKG;
            copiedFileList = performCopyFiles(gpkfiles, tempDir, dataset.getId(), true, "gpkg");
        } else {
            copiedFileList = performCopyFiles(gpkfiles, tempDir, "", false, "");
        }

        SimpleFeatureCollection inputFeatures = getSimpleFeatureCollectionFromFile(copiedFileList.get(0));

        SimpleFeatureType sft = inputFeatures.getSchema();

        SimpleFeatureTypeBuilder sftBuilder = new SimpleFeatureTypeBuilder();
        sftBuilder.init(sft);

        // make sure that name of feature type is matched with name file file
        sftBuilder.setName(dataset.getId());

        for (int i = 0; i < csvHeaders.length; i++) {
            if (i != csvIdLoc) {
                AttributeTypeBuilder build = new AttributeTypeBuilder();
                build.setNillable(false);
                build.setBinding(String.class);
                build.setLength(55);    // currently it has been set to 55 but needs to be discussed
                sftBuilder.add(build.buildDescriptor(csvHeaders[i]));
            }
        }
        SimpleFeatureType newSft = sftBuilder.buildFeatureType();

        DefaultFeatureCollection newCollection = new DefaultFeatureCollection();

        FeatureIterator<SimpleFeature> inputFeatureIterator = inputFeatures.features();

        // figure out the unique id column location
        int shpUniqueColLoc = 0;
        List<AttributeDescriptor> ads = inputFeatures.getSchema().getAttributeDescriptors();
        for (int i = 0; i < ads.size(); i++) {
            if (ads.get(i).getLocalName().equalsIgnoreCase(UNI_ID_SHP)) {
                shpUniqueColLoc = i;
            }
        }

        try {
            while (inputFeatureIterator.hasNext()) {
                SimpleFeature inputFeature = inputFeatureIterator.next();
                SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(newSft);
                sfb.init(inputFeature);
                String csvConnector = inputFeature.getAttribute(shpUniqueColLoc).toString();
                // find matching csv rows
                String[] matchedCsvRow = null;
                for (int j = 0; j < csvRows.size(); j++) {
                    if (csvRows.get(j)[csvIdLoc].equals(csvConnector)) {
                        matchedCsvRow = csvRows.get(j);
                        csvRows.remove(j);
                    }
                }
                // insert the values in the new column
                if (matchedCsvRow != null) {
                    for (int j = 0; j < csvHeaders.length; j++) {
                        if (j != csvIdLoc) {
                            sfb.set(csvHeaders[j], matchedCsvRow[j]);
                        }
                    }
                }
                SimpleFeature newFeature = sfb.buildFeature(null);
                newCollection.add(newFeature);
            }
        } finally {
            inputFeatureIterator.close();
        }

        // return outToFile(new File(tempDir + File.separator + outFileName), newSft, newCollection);
        return outToGpkgFile(new File(tempDir + File.separator + dataset.getId() + "." + FileUtils.EXTENSION_GEOPACKAGE), newCollection);
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
    public static List<File> performCopyFiles(List<File> fileList, String tempDir, String datasetId, boolean isGeoserver, String inExt) throws IOException {
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
                } else if (inExt.equalsIgnoreCase("gpkg")) {
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

    public static List<File> performCopyNetworkFiles(Dataset dataset, List<File> fileList, String tempDir, String datasetId,
                                                     boolean isGeoserver, String inExt) throws IOException {
        List<File> outList = new ArrayList<File>();
        String linkName = FilenameUtils.removeExtension(dataset.getNetworkDataset().getLink().getFileName());
        String nodeName = FilenameUtils.removeExtension(dataset.getNetworkDataset().getNode().getFileName());

        for (int i = 0; i < fileList.size(); i++) {
            File sourceFile = fileList.get(i);
            String fileName = FilenameUtils.getName(sourceFile.getName());
            String fileNameNoExt = FilenameUtils.removeExtension(fileName);
            String fileExt = "";
            if (isGeoserver) {
                fileExt = FilenameUtils.getExtension(sourceFile.getName());
                File newFile = null;
                if (fileNameNoExt.equalsIgnoreCase(linkName)) {
                    newFile = new File(sourceFile.getParent() + File.separator + datasetId + "_link." + fileExt);
                    fileName = FilenameUtils.getName(newFile.getName());
                } else if (fileNameNoExt.equalsIgnoreCase(nodeName)) {
                    newFile = new File(sourceFile.getParent() + File.separator + datasetId + "_node." + fileExt);
                    fileName = FilenameUtils.getName(newFile.getName());
                }
            }
            File destFile = new File(tempDir + File.separator + fileName);
            if (isGeoserver) {
                if (inExt.equalsIgnoreCase("shp")) {
                    if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("dbf") ||
                        fileExt.equalsIgnoreCase("shx") || fileExt.equalsIgnoreCase("prj")) {
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
     * copy file in the list to the temporary directory
     *
     * @param sourceFile
     * @param tempDir
     * @return
     * @throws IOException
     */
    public static List<File> performUnzipShpFile(File sourceFile, String tempDir) throws IOException {
        String fileName = FilenameUtils.getName(sourceFile.getName());

        // following line can be used when move the file to different folder and unzip
        // File destFile = new File(tempDir + File.separator + fileName);
        // org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
        // List<File>  outList = unzipShapefiles(destFile, new File(destFile.getParent()));

        List<File> outList = unzipShapefiles(sourceFile, new File(sourceFile.getParent()));

        return outList;
    }

    /**
     * unzip zipped shapefile and check if it is complete shapefile
     *
     * @param file
     * @param destDirectory
     * @return
     */
    public static List<File> unzipShapefiles(File file, File destDirectory) {
        int shapefileCompoentIndex = 0;
        List<File> outList = new ArrayList<File>();
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

                // check shapefile components
                String fileExt = FilenameUtils.getExtension(newFile.getName());
                if (fileExt.equalsIgnoreCase("shp")) {
                    shapefileCompoentIndex += 1;
                } else if (fileExt.equalsIgnoreCase("shx")) {
                    shapefileCompoentIndex += 1;
                } else if (fileExt.equalsIgnoreCase("dbf")) {
                    shapefileCompoentIndex += 1;
                } else if (fileExt.equalsIgnoreCase("prj")) {
                    shapefileCompoentIndex++;
                }

                outList.add(newFile);
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            // remove temp directory used for unzip
            ArrayList<File> files = new ArrayList<File>(Arrays.asList(destDirectory.listFiles()));
            FileUtils.deleteTmpDir(files);

            logger.error("Failed to process zip file. " +
                "Zip file structure might not be flat with folder tree structure.", e);
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Failed to process zip file. " +
                "Please check if the zip file structure is flat without no folders or tree structure.");
        }

        // check if there are all the shapefile components
        if (shapefileCompoentIndex != 4) {
            // remove temp directory used for unzip
            FileUtils.deleteTmpDir(outList.get(0));

            logger.error("The zipfile is not a complete shapefile.");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The zipfile is not a complete shapefile. " +
                "Please check if there is .shp, .shx, .dbf, and .prj file in the zip file");
        } else {
            return outList;
        }
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
            logger.error(typeName + " does not support read/write access");
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
     * create actual output geopackage in the directory
     *
     * @param geoPkgPathFile
     * @param collection
     * @return
     * @throws IOException
     */
    public static File outToGpkgFile(File geoPkgPathFile, DefaultFeatureCollection collection)
        throws IOException {

        GeoPackage geopkg = new GeoPackage(geoPkgPathFile);
        geopkg.init();

        // adding features to geopkg
        FeatureEntry entry = new FeatureEntry();
        geopkg.add(entry, collection);

        geopkg.close();

        return geoPkgPathFile;
    }

    /**
     * read csv file
     *
     * @param inCsv
     * @return
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static List<String[]> readCsvFile(File inCsv) throws IOException, CsvException {
	CSVParser parser = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build();
	CSVReader reader = new CSVReaderBuilder(new FileReader(inCsv)).withSkipLines(1).withCSVParser(parser).build();
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
     * create GUID filed in network dataset
     *
     * @param dataset
     * @param shpfiles
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean createGUIDinShpfile(Dataset dataset, List<File> shpfiles, String fileName) throws IOException {
        // create file list for file
        List<File> files = new LinkedList<>();
        String fileNameNoExt = FilenameUtils.removeExtension(fileName);
        for (File infile : shpfiles) {
            String tmpFileName = FilenameUtils.removeExtension(FilenameUtils.getName(infile.getName()));
            if (tmpFileName.equalsIgnoreCase(fileNameNoExt)) {
                files.add(infile);
            }
        }
        boolean isLink = createGUIDinShpfile(dataset, files);

        return isLink;
    }

    /**
     * check if GUID field is in the shapefile
     *
     * @param dataset
     * @param shpfiles
     * @return
     * @throws IOException
     */
    public static boolean isGUIDinShpfile(Dataset dataset, List<File> shpfiles) throws IOException {
        // set geometry factory
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        String outFileName = FilenameUtils.getBaseName(shpfiles.get(0).getName()) + "." + FileUtils.EXTENSION_SHP;

        // create temp dir and copy files to temp dir
        String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
        List<File> copiedFileList = performCopyFiles(shpfiles, tempDir, "", false, "shp");
        boolean isGuid = false;

        SimpleFeatureCollection inputFeatures = getSimpleFeatureCollectionFromFileList(copiedFileList);

        // check if there is guid exists
        isGuid = doesGuidExist(inputFeatures);

        // remove temp dir after checking the file
        FileUtils.deleteTmpDir(copiedFileList.get(0));

        return isGuid;
    }

    public static boolean isGUIDinGeopackage(SimpleFeatureCollection sfc) throws IOException {
        // check if there is guid exists
        boolean isGuid = doesGuidExist(sfc);

        return isGuid;
    }

    /**
     * check if GUID field is in the shapefile
     *
     * @param dataset
     * @param shpfiles
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean isGUIDinShpfile(Dataset dataset, List<File> shpfiles, String fileName) throws IOException {
        // create file list for file
        List<File> files = new LinkedList<>();
        String fileNameNoExt = FilenameUtils.removeExtension(fileName);
        for (File infile : shpfiles) {
            String tmpFileName = FilenameUtils.removeExtension(FilenameUtils.getName(infile.getName()));
            if (tmpFileName.equalsIgnoreCase(fileNameNoExt)) {
                files.add(infile);
            }
        }
        boolean isGuid = isGUIDinShpfile(dataset, files);

        return isGuid;
    }

    /**
     * create GUID field in the shapefile
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
        List<File> copiedFileList = performCopyFiles(shpfiles, tempDir, "", false, "shp");
        boolean isGuid = false;

        SimpleFeatureCollection inputFeatures = getSimpleFeatureCollectionFromFileList(copiedFileList);

        // check if there is guid exists
        isGuid = doesGuidExist(inputFeatures);
        // creaste new dbf in the temp dir and move it to data repo directory
        if (isGuid != true) {
            logger.debug("Creating GUID field in the dataset for " + outFileName);
            SimpleFeatureType sft = inputFeatures.getSchema();

            SimpleFeatureTypeBuilder sftBuilder = new SimpleFeatureTypeBuilder();
            sftBuilder.init(sft);

            AttributeTypeBuilder build = new AttributeTypeBuilder();
            build.setNillable(false);
            build.setBinding(String.class);
            build.setLength(36);    // UUID length is fixed to 36
            sftBuilder.add(build.buildDescriptor(UNI_ID_SHP));
            SimpleFeatureType newSft = sftBuilder.buildFeatureType();

            DefaultFeatureCollection newCollection = new DefaultFeatureCollection();

            FeatureIterator<SimpleFeature> inputFeatureIterator = inputFeatures.features();
            try {
                while (inputFeatureIterator.hasNext()) {
                    SimpleFeature inputFeature = inputFeatureIterator.next();
                    SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(newSft);
                    sfb.init(inputFeature);
                    UUID uuid = UUID.randomUUID();

                    sfb.set(UNI_ID_SHP, uuid.toString());
                    SimpleFeature newFeature = sfb.buildFeature(null);
                    newCollection.add(newFeature);
                }

                File outFile = outToFile(new File(tempDir + File.separator + outFileName), newSft, newCollection);
                FileUtils.switchDbfFile(outFile, shpfiles);
            } finally {
                inputFeatureIterator.close();
            }
        }

        FileUtils.deleteTmpDir(copiedFileList.get(0));

        return !isGuid;
    }

    /**
     * check if the guid exists
     *
     * @param inputFeatures
     * @return
     */
    public static boolean doesGuidExist(SimpleFeatureCollection inputFeatures) {
        boolean isGuid = false;
        SimpleFeatureIterator inputFeatureIterator = inputFeatures.features();
        try {
            SimpleFeature inputFeature = inputFeatureIterator.next();
            for (int i = 0; i < inputFeature.getAttributeCount(); i++) {
                AttributeDescriptor attributeType = inputFeature.getFeatureType().getDescriptor(i);
                if (attributeType.getLocalName().equalsIgnoreCase(UNI_ID_SHP)) {
                    isGuid = true;
                    break;
                }
            }
        } finally {
            inputFeatureIterator.close();
        }

        return isGuid;
    }

    /**
     * check if geopackage has single layer
     * and layer name is the same as the file name
     * and layer is not raster
     * since incore-services doesn't support raster geopackage yet
     *
     * @param inFile
     * @return
     * @throws IOException
     */
    public static GeoUtils.gpkgValidationResult isGpkgFitToService(File inFile) throws IOException {
        int output = 0;
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
            map.put(GeoPkgDataStoreFactory.DATABASE.key, inFile.getAbsoluteFile());
            DataStore dataStore = DataStoreFinder.getDataStore(map);

            if (dataStore == null) {
                throw new IOException("Unable to open geopackage file");
            }

            // get all layer names in input geopackage file
            // if the layerNames list is more than one, it means there are multiple vector layer
            // if the layerNames list empty then, there is no vector layer or it could be a raster data
            String[] layerNames = dataStore.getTypeNames();

            if (layerNames.length == 1) {
                // check if the layername is the same as file name
                String layerName = layerNames[0];
                String fileName = inFile.getName().split("\\.")[0];
                if (!layerName.equals(fileName)) {
                    return GeoUtils.gpkgValidationResult.NAME_MISMATCH;
                }
            } else if (layerNames.length == 0) {
                return GeoUtils.gpkgValidationResult.RASTER_OR_NO_VECTOR_LAYER;
            } else if (layerNames.length > 1) {
                return GeoUtils.gpkgValidationResult.MULTIPLE_VECTOR_LAYERS;
            }
        } catch (IOException e) {
            throw new IOException("Unable to open geopackage file.");
        }

        return GeoUtils.gpkgValidationResult.VALID;
    }

    public static DataStore connectToPostGIS(String host, String port, String database, String user, String password) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", host);
        params.put("port", Integer.parseInt(port));
        params.put("database", database);
        params.put("user", user);
        params.put("passwd", password);
        return DataStoreFinder.getDataStore(params);
    }

    public static Filter buildFipsFilter(List<String> fipsCodes) throws Exception {
        String filterStr = fipsCodes.stream()
            .map(code -> "cbfips LIKE '" + code + "%'")
            .reduce((a, b) -> a + " OR " + b)
            .orElse("FALSE");
        return CQL.toFilter(filterStr);
    }

    public static Filter buildBoundingBoxFilter(List<List<Double>> boundingBox) throws Exception {
        if (boundingBox.size() != 2) {
            throw new IllegalArgumentException("Bounding box must contain exactly 2 coordinate pairs.");
        }

        List<Double> bottomLeft = boundingBox.get(0);
        List<Double> topRight = boundingBox.get(1);

        if (bottomLeft.size() != 2 || topRight.size() != 2) {
            throw new IllegalArgumentException("Each bounding box coordinate must be a pair of [lat, lon].");
        }

        double minY = bottomLeft.get(0); // lat1
        double minX = bottomLeft.get(1); // lon1
        double maxY = topRight.get(0);   // lat2
        double maxX = topRight.get(1);   // lon2

        String cql = String.format("BBOX(geom, %f, %f, %f, %f)", minX, minY, maxX, maxY);
        return CQL.toFilter(cql);
    }


    public static SimpleFeatureType createRegulatedSchema(SimpleFeatureType originalSchema, String baseName) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(baseName);
        builder.setCRS(originalSchema.getCoordinateReferenceSystem());

        AttributeTypeBuilder attrBuilder = new AttributeTypeBuilder();
        String defaultGeometryName = null;

        for (AttributeDescriptor descriptor : originalSchema.getAttributeDescriptors()) {
            String name = descriptor.getLocalName();

            if (descriptor instanceof GeometryDescriptor) {
                builder.add(name, Point.class);
                builder.setDefaultGeometry(name);
                defaultGeometryName = name;
            } else if ("no_stories".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(3);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("year_built".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(4);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("appr_bldg".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(10);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("sq_foot".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(10);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("cont_val".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(10);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("archetype".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(4);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("arch_flood".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(4);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("arch_flood".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(4);
                builder.add(attrBuilder.buildDescriptor(name));
            } else if ("arch_sw".equals(name)) {
                attrBuilder.setBinding(Integer.class);
                attrBuilder.setLength(4);
                builder.add(attrBuilder.buildDescriptor(name));
            } else {
                builder.add(descriptor);
            }
        }

        if (defaultGeometryName == null) {
            throw new IllegalStateException("No geometry field found in schema.");
        }

        return builder.buildFeatureType();
    }

    public static void writeFeaturesToShapefile(SimpleFeatureCollection features, SimpleFeatureType schema, File shpFile) throws IOException {
        Map<String, Serializable> shpParams = new HashMap<>();
        shpParams.put("url", shpFile.toURI().toURL());
        shpParams.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore shpDataStore = (ShapefileDataStore) factory.createNewDataStore(shpParams);
        shpDataStore.createSchema(schema);

        Transaction tx = new DefaultTransaction("create");
        try (
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = shpDataStore.getFeatureWriterAppend(shpDataStore.getTypeNames()[0], tx);
            SimpleFeatureIterator iterator = features.features()
        ) {
            while (iterator.hasNext()) {
                SimpleFeature source = iterator.next();
                SimpleFeature target = writer.next();

                for (Property property : source.getProperties()) {
                    String name = property.getName().toString();
                    Object value = property.getValue();

                    if (name.equals(schema.getGeometryDescriptor().getLocalName())) {
                        target.setDefaultGeometry(value);
                    } else if (schema.getDescriptor(name) != null) {
                        target.setAttribute(name, value);
                    }
                }

                writer.write();
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException("Failed to write shapefile: " + e.getMessage(), e);
        } finally {
            tx.close();
        }
    }

    public static File zipDirectory(File directory, String baseName) throws IOException {
        File zipFile = new File(directory.getParent(), baseName + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : Objects.requireNonNull(directory.listFiles())) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
        return zipFile;
    }

    public static void cleanupDirectory(File directory) {
        if (directory != null && directory.exists()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                file.delete();
            }
            directory.delete();
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static Map<String, Object> buildDatasetPayload(String title, String description, String creator) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("creator", creator);
        payload.put("dataType", "ergo:buildingInventoryVer6");
        payload.put("format", "shapefile");
        return payload;
    }
}
