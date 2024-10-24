package edu.illinois.ncsa.incore.service.semantics.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Point;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.data.Transaction;

import java.io.*;
import java.util.*;

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
