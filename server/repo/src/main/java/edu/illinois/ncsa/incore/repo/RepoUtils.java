package edu.illinois.ncsa.incore.repo;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.util.JSON;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ywkim on 6/8/2017.
 */
public class RepoUtils {
    public static final String[] EXTENSIONS_SHAPEFILES = new String[]{"dbf", "prj", "shp", "shx"};
    public static final String EXTENSION_SHP = "shp";
    public static final String EXTENSION_META = "mvz";
    public static final String EXTENSION_CSV ="csv";
    public static final int INDENT_SPACE = 4;
    public static final String TAG_PROPERTIES_GIS = "gis-dataset-properties";
    public static final String TAG_PROPERTIES_MAP = "mapped-dataset-properties";
    public static final String TAG_PROPERTIES_FILE = "file-dataset-properties";
    public static final String TAG_PROPERTIES_RASTER = "raster-dataset-properties";
    public static final String TAG_PROPERTIES_SCENARIO = "dataset-properties";
    public static final String TAG_NAME = "name";
    public static final String TAG_VERSION = "version";
    public static final String TAG_DATA_FORMAT = "data-format";
    public static final String TAG_TYPE_ID = "type-id";
    public static final String TAG_FEATURE_TYPE_NAME = "feature-type-name";
    public static final String TAG_CONVERTED_FEATURE_TYPE_NAME = "converted-feature-type-name";
    public static final String TAG_GEOMETRY_TYPE = "geometry-type";
    public static final String TAG_LOCATION ="location";
    public static final String TAG_DESCRIPTION = "desription";
    public static final String TAG_DATASET_ID = "dataset-id";
    public static final String TAG_MAEVIZ_MAPPING = "maeviz-mapping";
    public static final String TAG_SCHEMA = "schema";
    public static final String TAG_MAPPING = "mapping";
    public static final String TAG_FROM = "from";
    public static final String TAG_TO = "to";
    public static final String TAG_METADATA = "metadata";
    public static final String TAG_TABLE_METADATA = "table-metadata";
    public static final String TAG_COLUMN_METADATA = "column-metadata";
    public static final String TAG_FRIENDLY_NAME = "friendly-name";
    public static final String TAG_FIELD_LENGTH = "field-length";
    public static final String TAG_UNIT = "unit";
    public static final String TAG_COLUMN_ID = "column-id";
    public static final String TAG_SIGFIGS = "sig-figs";
    public static final String TAG_UNIT_TYPE = "unit-type";
    public static final String TAG_IS_NUMERIC = "is-numeric";
    public static final String TAG_IS_RESULT = "is-result";
    public static final String TAG_PROPERTIES = "";
    public static final int TYPE_NUMBER_SHP = 1;
    public static final int TYPE_NUMBER_CSV = 2;
    public static final int TYPE_NUMBER_META = 3;
    public static final int TYPE_NUMBER_MULTI = 10;

    public static final Logger logger = Logger.getLogger(RepoUtils.class);


    public static void deleteTmpDir(File metadataFile, String fileExt) {
        String fileName = metadataFile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(metadataFile.separator));
        int extLoc = metadataFile.getName().indexOf(".");
        String extName = metadataFile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        String delFileName = fileNameWithNoExt + "." + fileExt;
        File delFile = new File(delFileName);
        deleteFiles(delFile, delFileName);

        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    public static void deleteTmpDir(File shapefile, String[] fileExts) {
        String fileName = shapefile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(shapefile.separator));
        int extLoc = shapefile.getName().indexOf(".");
        String extName = shapefile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        for (String extension : fileExts) {
            String delFileName = fileNameWithNoExt + "." + extension;
            File delFile = new File(delFileName);
            deleteFiles(delFile, delFileName);
        }
        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    public static void deleteFiles(File delFile, String delFileName){
        try {
            if (delFile.delete()) {
                logger.debug("file or directory deleted: " + delFileName);
            } else {
                logger.error("file or directory did not deleted: " + delFileName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // load shapefile data from repository web site
    public static String loadFileNameFromRepository(String inId, String extStr, String repoUrl) throws IOException {
        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        List<String> fileList = createFileListFromUrl(datasetUrl);

        String outfileStr = "";
        for (int i=0; i < fileList.size();i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals(extStr)) {
                outfileStr = fileList.get(i);
            }
        }

        String outfileName = "";
        if (outfileStr.length() > 0) {
            // get the base name of the shapefile
            String shapefileNames[] = outfileStr.split("." + extStr);
            String baseName = shapefileNames[0];
            String tempDir = Files.createTempDirectory("repo_download_").toString();
            if (extStr.equals(EXTENSION_SHP)) {
                for (String extension : EXTENSIONS_SHAPEFILES) {
                    HttpDownloader.downloadFile(datasetUrl + baseName + "." + extension, tempDir);
                }
            } else {
                HttpDownloader.downloadFile(datasetUrl + baseName + "." + extStr, tempDir);
            }
            outfileName = tempDir + File.separator + baseName + "." + extStr;
        }

        return outfileName;
    }

    public static List<String> createFileListFromUrl(String inUrl) {
        String realUrl = getRealUrl(inUrl);
        List<String> linkList = getDirList(realUrl);

        return linkList;
    }

    public static List<String> getDirList(String inUrl){
        List<String> linkList = new LinkedList<String>();
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect(inUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements links = doc.select("a");
        String linkAtr = "";

        for (int i=0;i < links.size();i++){
            linkAtr = links.get(i).attr("href");
            if (linkAtr.length() > 3) {
                linkList.add(linkAtr);
            }
        }

        return linkList;
    }

    public static String getRealUrl(String inUrl) {
        String strs[] = inUrl.split("/converted/");
        String urlPrefix = strs[0];
        String realUrl = urlPrefix + "/converted/";

        return realUrl;
    }

    // ingest csv file into mongodb
    public static boolean ingestMetaToMongo(String extStr, String typeId, String datasetId, String mongoUrl, String geoDbName, String repoUrl, String serverUrlPrefix){
        MongoDatabase database = getMongoDatabase(mongoUrl, geoDbName);

        // check if the dataset id is already in the mongodb
        boolean isCollExist = isCollectionExist(database, typeId);

        // create collection is collection is not there
        if (!isCollExist) {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
            // insert document into collection
            String outJson = getMetaJson(typeId, datasetId, repoUrl, serverUrlPrefix);
            return insertJsonStringToMongo(database, typeId, datasetId, outJson);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists.");
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String outJson = getMetaJson(typeId, datasetId, repoUrl, serverUrlPrefix);
                return insertJsonStringToMongo(database, typeId, datasetId, outJson);
            } else {
                logger.debug("Document already exists");
                return false;
            }
        }
    }

    // ingest json string into mongodb
    public static boolean ingestJsonStringToMongo(String inJson, String collId, String docId, String mongoUrl, String dbName) {
        MongoDatabase database = getMongoDatabase(mongoUrl, dbName);

        // check if the dataset id is already in the mongodb
        boolean isCollExist = isCollectionExist(database, collId);

        if (!isCollExist) { // create collection and insert input json as document
            database.createCollection(collId, new CreateCollectionOptions().capped(false));
            return insertJsonStringToMongo(database, collId, docId, inJson);
        } else {
            // check if the document already exists
            boolean isDocExist = isDocumentExist(database, collId, docId);
            if (!isDocExist) {
                // insert document into collection
                return insertJsonStringToMongo(database, collId, docId, inJson);
            } else {
                logger.debug("Document already exists");
                return false;
            }
        }
    }

    // ingest csv file into mongodb
    public static boolean ingestCsvToMongo(String extStr, String typeId, String datasetId, String mongoUrl, String geoDbName, String repoUrl, String serverUrlPrefix){
        MongoDatabase database = getMongoDatabase(mongoUrl, geoDbName);

        // check if the dataset id is already in the mongodb
        boolean isCollExist = isCollectionExist(database, typeId);

        // create collection if collection is not there
        if (!isCollExist) {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
            // insert document into collection
            String outJson = getCsvJson(typeId, datasetId, repoUrl);
            return insertCsvJsonToMongo(database, typeId, datasetId, outJson);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists.");
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String outJson = getCsvJson(typeId, datasetId, repoUrl);
                return insertCsvJsonToMongo(database, typeId, datasetId, outJson);
            } else {
                logger.debug("Document already exists");
                return false;
            }
        }
    }

    // ingest shapefile into mongodb
    public static boolean ingestShpfileToMongo(String typeId, String datasetId, String mongoUrl, String geoDbName, String repoUrl){
        MongoDatabase database = getMongoDatabase(mongoUrl, geoDbName);

        // check if the dataset id is already in the mongodb
        boolean isCollExist = isCollectionExist(database, typeId);

        // create collection is collection is not there
        if (!isCollExist) {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
            // insert document into collection
            String geoJson = getGeoJson(typeId, datasetId, repoUrl);
            return insertGeoJsonToMongo(database, typeId, datasetId, geoJson);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists.");
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String geoJson = getGeoJson(typeId, datasetId, repoUrl);
                return insertGeoJsonToMongo(database, typeId, datasetId, geoJson);
            } else {
                logger.debug("Document already exists");
                return false;
            }
        }
    }

    // create json from the csv file
    public static String getCsvJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";
        String outJson = "";
        String fileName = "";
        try{
            fileName = loadFileNameFromRepository(combinedId, EXTENSION_CSV, repoUrl);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatCsvAsJson(dataset, combinedId);
            }
        }catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
        return outJson;
    }

    // create json from the csv file
    public static String getMetaJson(String typeId, String datasetId, String repoUrl, String serverUrlPrefix) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId;
        String datasetUrl = repoUrl + typeId + "/";
        String outJson = "";
        String fileName = "";
        try{
            String tempDir = Files.createTempDirectory("repo_download_").toString();
            HttpDownloader.downloadFile(datasetUrl + datasetId + "." + EXTENSION_META, tempDir);
            fileName = tempDir + File.separator + datasetId + "." + EXTENSION_META;
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatMetadataAsJson(dataset, combinedId, serverUrlPrefix);
            }
        }catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
        return outJson;
    }

    // create geoJson from the shapefile url
    public static String getGeoJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";
        String outJson = "";
        String fileName = "";
        try{
            fileName = loadFileNameFromRepository(combinedId, EXTENSION_SHP, repoUrl);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatDatasetAsGeoJson(dataset);
            }
        }catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
        return outJson;
    }

    // validate if json is okay
    public static boolean isJSONValid(String inJson) {
        try {
            new JSONObject(inJson);
        } catch (JSONException ex) {
            try {
                new JSONArray(inJson);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static MongoDatabase getMongoDatabase(String mongoUrl, String dbName){
        //crete mongodb connection
        MongoClientURI mongoUri = new MongoClientURI(mongoUrl);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase database = mongoClient.getDatabase(dbName);

        return database;
    }

    public static List<String> getDocListByCollId(String mongoUrl, String dbName, String id) {
        MongoDatabase database = getMongoDatabase(mongoUrl, dbName);
        MongoIterable<String> collNames = database.listCollectionNames();
        List<String> outList = new LinkedList<String>();
        for (String collName: collNames) {
            outList.add(collName);
        }
        return  outList;
    }

    // check if collection exists, otherwise create one with a type id
    public static boolean isCollectionExist(MongoDatabase database, String collectionName) {
        boolean isCollExist = false;
        MongoIterable<String> collNames = database.listCollectionNames();

        for (String collName: collNames) {
            if (collName.equalsIgnoreCase(collectionName)) {
                isCollExist = true;
            }
        }
        return isCollExist;
    }

    // check if the document with dataset name exist in the collection
    public static boolean isDocumentExist(MongoDatabase database, String collectionName, String docName ){
        boolean isDocExist = false;
        MongoCollection<Document> tmpCollection = database.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", docName);
        FindIterable existDoc = tmpCollection.find(query);

        if (existDoc.first() != null) {
            isDocExist = true;
        }
        return isDocExist;
    }

    public static boolean insertGeoJsonToMongo(MongoDatabase database, String typeId, String datasetId, String inJson) {
        try {
            MongoCollection<org.bson.Document> geoJsonColl = database.getCollection(typeId);
            BasicDBObject document = (BasicDBObject) JSON.parse(inJson);
            document.put("_id", datasetId);
            geoJsonColl.insertOne(new org.bson.Document(document));
            geoJsonColl.createIndex(new BasicDBObject("geometry","2dsphere"));
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }

    public static boolean insertCsvJsonToMongo(MongoDatabase database, String collId, String docId, String inJson) {
        try {
            MongoCollection<org.bson.Document> csvJsonColl = database.getCollection(collId);
            BasicDBList docList = (BasicDBList) JSON.parse(inJson);
            org.bson.Document document = new org.bson.Document();
            document.put("_id", docId);
            document.put("table", docList);
            csvJsonColl.insertOne(document);
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }

    public static boolean insertJsonStringToMongo(MongoDatabase database, String collId, String docId, String inJson) {
        try {
            MongoCollection<org.bson.Document> collName = database.getCollection(collId);
            BasicDBObject docObj = (BasicDBObject) JSON.parse(inJson);
            docObj.put("_id", docId);
            collName.insertOne(new Document(docObj));
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }

    public static String getJsonByDatasetIdFromMongo(String datasetId, String mongoUrl, String geoDbName){
        MongoDatabase database = getMongoDatabase(mongoUrl, geoDbName);
        MongoIterable<String> collNames = database.listCollectionNames();
        org.bson.Document result = new Document();
        String outJson = "";

        for (String collectionName: collNames) {
            MongoCollection<Document> tmpCollection = database.getCollection(collectionName);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", datasetId);
            FindIterable existDoc = tmpCollection.find(query);

            if (existDoc.first() != null) {
                result = (org.bson.Document) existDoc.first();
                JsonWriterSettings writerSettings = new JsonWriterSettings(JsonMode.SHELL, true);
                outJson = result.toJson(writerSettings);
                return outJson;
            }
        }

        return outJson;
    }

    public void insertGeoJsonToMongoTest(String typeId, String datasetId, String inJson, String mongoUrl, String geoDbName) {
        //crete mongodb connection
        MongoClientURI mongoUri = new MongoClientURI(mongoUrl);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase database = mongoClient.getDatabase(geoDbName);

        // check if collection exists, otherwise create one with a type id
        MongoIterable<String> collNames = database.listCollectionNames();
        boolean isCollExist = false;
        for (String collName: collNames) {
            if (collName.equalsIgnoreCase(typeId)) {
                isCollExist = true;
            }
        }

        // create collection
        if (isCollExist) {
            logger.debug("Collection already exists");
        } else {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
        }

        MongoCollection<org.bson.Document> geoJsonColl = database.getCollection(typeId);
        BasicDBObject document = (BasicDBObject) JSON.parse(inJson);
        document.put("_id", datasetId);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", datasetId);
        FindIterable existDoc = geoJsonColl.find(document);
        //MongoIterable<Document> iterable = geoJsonColl.find({_id: datasetId), {_id:1}).limit(1);
        if (existDoc.first() == null) {
            geoJsonColl.insertOne(new org.bson.Document(query));
        } else {
            logger.debug("Document already exists");
        }

//        org.bson.Document myDoc = geoJsonColl.find(query).first();
//        String user; // the user name
//        String database; // the name of the database in which the user is defined
//        char[] password; // the password as a character array
//        MongoCredential credential = MongoCredential.createCredential(user, database, password);
//        MongoClientOptions options = MongoClientOptions.builder().sslEnabled(true).build();
//        MongoClient mongoClient = new MongoClient(new ServerAddress("host1", 27017), Arrays.asList(credential), options);
    }

    public static String extractValueFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        if (jsonObj.has(inId)) {
            Object output = jsonObj.get(inId);
            return output.toString();
        } else {
            return "";
        }
    }

    public static String formatDatasetAsGeoJson(File shapefile) throws IOException {
        //TODO: this should return the data in geoJSON format
        String geoJsonStr;

        shapefile.setReadOnly();

        ShapefileDataStore store = new ShapefileDataStore(shapefile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJsonStr = writer.toString();
        }

        RepoUtils.deleteTmpDir(shapefile, EXTENSIONS_SHAPEFILES);

        return geoJsonStr;
    }

    public static String formatMetadataAsJson(File metadataFile, String inId, String serverUrlPrefix) throws IOException {
        // convert from UTF-16 to UTF-8
        String xmlString = "";
        metadataFile.setReadOnly();
        Reader metadataReader = new InputStreamReader(new FileInputStream(metadataFile), "UTF-16");
        char metaCharBuffer[] = new char[2048];
        int len;
        while ((len = metadataReader.read(metaCharBuffer, 0, metaCharBuffer.length)) != -1) {
            xmlString = xmlString + new String(metaCharBuffer, 0, len);
        }
        metadataReader.close();
        RepoUtils.deleteTmpDir(metadataFile, EXTENSION_META);

        // remove metadata file extestion from inId if there is any
        String tmpEndStr = inId.substring(inId.lastIndexOf('.') + 1);
        if (tmpEndStr.equals(EXTENSION_META)) {
            inId = inId.substring(0, inId.length() - 4);
        }

        try {
            JSONObject metaJsonObj = XML.toJSONObject(xmlString);
            JSONObject locObj = null;
            if (metaJsonObj.has(TAG_PROPERTIES_GIS)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_GIS).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_MAP)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_MAP).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_FILE)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_FILE).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_RASTER)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_RASTER).getJSONObject(TAG_DATASET_ID);
            }
            if (metaJsonObj.has(TAG_PROPERTIES_SCENARIO)) {
                locObj = metaJsonObj.getJSONObject(TAG_PROPERTIES_SCENARIO).getJSONObject(TAG_DATASET_ID);
            }

            String newUrl = serverUrlPrefix + inId + "/files";
            locObj.put(TAG_LOCATION, newUrl);
            String jsonString = metaJsonObj.toString(INDENT_SPACE);
            return jsonString;
        } catch (JSONException ex) {
            logger.error(ex);
            return "{\"error:\" + \"" + ex.getLocalizedMessage() + "\"}";
        }
    }

    // check what kind of file format is in the repository web site.
    public static int checkDataFormatFromRepository(String inId, String repoUrl) {
        int typeNumber  = 0;    // 1: shp, 2: csv, 3: mvz
        boolean isMultiType = false;

        String urlPart = inId.replace("$", "/");
        String datasetUrl = repoUrl + urlPart;
        List<String> fileList = createFileListFromUrl(datasetUrl);

        for (int i=0; i < fileList.size();i++) {
            String fileExt = FilenameUtils.getExtension(fileList.get(i));
            if (fileExt.equals(EXTENSION_SHP)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_SHP;
            }
            if (fileExt.equals(EXTENSION_CSV)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_CSV;
            }
            if (fileExt.equals(EXTENSION_META)) {
                if (typeNumber > 0) {
                    isMultiType = true;
                }
                typeNumber = TYPE_NUMBER_META;
            }
        }

        // if there are multiple type of file extensions return high number
        if (isMultiType) {
            typeNumber = TYPE_NUMBER_MULTI;
        }
        return typeNumber;
    }

    public static String formatCsvAsJson(File inCsv, String inId) throws IOException {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(csvSchema).readValues(inCsv);

        List<Map<?, ?>> data =  mappingIterator.readAll();
        ObjectMapper mapper = new ObjectMapper();
        String outStr = mapper.writeValueAsString(data);

        RepoUtils.deleteTmpDir(inCsv, EXTENSION_CSV);

        return outStr;
    }
}
