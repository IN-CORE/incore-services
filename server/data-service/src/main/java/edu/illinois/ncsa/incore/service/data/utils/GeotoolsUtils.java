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

import com.opencsv.CSVReader;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import org.locationtech.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
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
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
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
        GridCoverage coverage = getGridCoverage(file);
        org.opengis.geometry.Envelope env = coverage.getEnvelope();

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

    public static GridCoverage getGridCoverage(File file) throws IOException {
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        GridCoverageReader reader = format.getReader(file);
        GridCoverage coverage = reader.read(null);
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
     * join csv file to shapefile and create a new geopackage file
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
            copiedFileList = performCopyFiles(shpfiles, tempDir, dataset.getId(), true, "shp");
        } else {
            copiedFileList = performCopyFiles(shpfiles, tempDir, "", false, "");
        }

        SimpleFeatureCollection inputFeatures = getSimpleFeatureCollectionFromFileList(copiedFileList);

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
                    if (csvRows.get(j)[csvIdLoc].toString().equals(csvConnector)) {
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

        if (isGuid) {
            return false;
        } else {
            return true;
        }
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
}

