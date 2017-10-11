package edu.illinois.ncsa.incore.service.data.geotools;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Created by ywkim on 10/5/2017.
 */
public class GeotoolsUtils {
    private static GeometryFactory geometryFactory;
    private static final String uniIdShp = "Id";
    private static final String uniIdCsv = "Id";

    private static String inSourceFile = "C:/Users/ywkim/Documents/NIST/Centerville/Building_zones.shp"; //$NON-NLS-1$
//    private static String inResultCsv = "C:/Users/ywkim/Documents/NIST/Centerville/table1.txt";
    private static String outDir = "C:/Users/Ywkim/Documents/shapefile/"; //$NON-NLS-1$
    private static String outFileName = "test.shp";

    public static void JoinTableShapefile(File shpFile, File csvFile) throws IOException {
        // set geometry factory
        geometryFactory = JTSFactoryFinder.getGeometryFactory();

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
            if (header.equals(uniIdCsv)) {
                csvIdLoc = i;
            }
        }

        // set dataset
        URL inSourceFileUrl = new URL("file:/" + inSourceFile);
        URL inSourceFileUrlTwo = shpFile.toURI().toURL();
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
                    if (attributeType.getLocalName() != "the_geom") {
                        resultMap.put(attributeType.getLocalName(), attribute);
                        // check if this is unique id
                        if (attributeType.getLocalName().equalsIgnoreCase(uniIdShp)) {
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
//							System.out.println(Arrays.toString(matchedCsvRow));
                            // add matched row to resultMap
                            if (matchedCsvRow != null) {
                                for (int j = 0; j < matchedCsvRow.length; j++) {
                                    if (j != csvIdLoc) {
                                        resultMap.put(csvHeaders[j], matchedCsvRow[j]);

                                    }
                                }
                            }
                        }
                        // System.out.println(attributeType.getLocalName() + " "
                        // + attribute);
                    }
                }
//				System.out.println(resultMap);
                resultMapList.add(resultMap);

                finalList.add(g);
                finalResultMapList.addAll(resultMapList);
            }
        } finally {
            inputFeatureIterator.close();
        }

        createOutfile(finalList, finalResultMapList, outDir);
    }

    @SuppressWarnings("unchecked")
    public static void createOutfile(List<Geometry> finalList, @SuppressWarnings("rawtypes") List<Map> resultMapList,
                                     String outDir) throws IOException {
        File outFile = new File(outDir + outFileName); //$NON-NLS-1$

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        System.out.println(finalList.get(0).getClass());

        AttributeTypeBuilder attBuilder = new AttributeTypeBuilder();
        attBuilder.setName("the_geom"); //$NON-NLS-1$
        attBuilder.setBinding(finalList.get(0).getClass());
        attBuilder.crs(DefaultGeographicCRS.WGS84);

        GeometryType geomType = attBuilder.buildGeometryType();
        GeometryDescriptor geomDesc = attBuilder.buildDescriptor("the_geom", geomType); //$NON-NLS-1$

        builder.setName("output"); //$NON-NLS-1$
        builder.add(geomDesc);
//		builder.add("NEW_UNI_ID", Integer.class); //$NON-NLS-1$	// to add new unique id column

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
        outToFile(outFile, schema, collection);
    }

    public static void outToFile(File pathFile, SimpleFeatureType schema, DefaultFeatureCollection collection)
            throws IOException {
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", pathFile.toURI().toURL()); //$NON-NLS-1$
        params.put("create spatial index", Boolean.TRUE); //$NON-NLS-1$
        params.put("enable spatial index", Boolean.TRUE); //$NON-NLS-1$
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(schema);

        Transaction transaction = new DefaultTransaction("create"); //$NON-NLS-1$

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
            // System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access"); //$NON-NLS-1$
            System.exit(1);
        }
    }

    public static DataStore getShapefileDataStore(URL shpUrl, Boolean spatialIndex) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", shpUrl); //$NON-NLS-1$
        map.put("create spatial index", spatialIndex); //$NON-NLS-1$
        map.put("enable spatial index", spatialIndex); //$NON-NLS-1$

        return DataStoreFinder.getDataStore(map);
    }

    @SuppressWarnings("resource")
    public static List<String[]> readCsvFile(File inCsv) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(inCsv), ',', '"', 1);
        List<String[]> rows = reader.readAll();
        return rows;
    }

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

}

