package edu.illinois.ncsa.incore.service.semantics.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
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
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GeotoolsUtils {
    private static final Logger logger = Logger.getLogger(GeotoolsUtils.class);

    public static SimpleFeatureType buildSchema(String[] headers, List<String> dTypes) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Geometry");
        builder.setCRS(DefaultGeographicCRS.WGS84);

        builder.add("Geometry", Point.class);
        String name = "";

        for (int i=0; i < dTypes.size(); i++) {
            name = headers[i];
            if (dTypes.get(i).equalsIgnoreCase("xsd:double")) {
                builder.add(name, Double.class);
            } else if (dTypes.get(i).equalsIgnoreCase("xsd:integer")) {
                builder.add(name, Integer.class);
            } else if (dTypes.get(i).equalsIgnoreCase("xsd:decimal")) {
                builder.add(name, Float.class);
            } else {
                builder.add(name, String.class);
            }
        }

        return builder.buildFeatureType();
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
}
