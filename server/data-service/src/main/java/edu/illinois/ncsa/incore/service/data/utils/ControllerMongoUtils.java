package edu.illinois.ncsa.incore.service.data.utils;

import com.google.gson.Gson;
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
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Sorts.descending;

/**
 * Created by ywkim on 9/27/2017.
 */
public class ControllerMongoUtils {
    public static final Logger logger = Logger.getLogger(ControllerMongoUtils.class);

    // ingest csv file into mongodb
    public static boolean ingestMetaToMongo(String extStr, String typeId, String datasetId, String mongoUrl, String geoDbName, String repoUrl, String serverUrlPrefix){
        MongoDatabase database = getMongoDatabase(mongoUrl, geoDbName);

        // check if the dataset id is already in the mongodb
        boolean isCollExist = isCollectionExist(database, typeId);

        // create collection is collection is not there
        if (!isCollExist) {
            database.createCollection(typeId, new CreateCollectionOptions().capped(false));
            // insert document into collection
            String outJson = ControllerJsonUtils.getMetaJson(typeId, datasetId, repoUrl, serverUrlPrefix);
            return insertJsonStringToMongo(database, typeId, datasetId, outJson, true);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists."); //$NON-NLS-1$ //$NON-NLS-2$
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String outJson = ControllerJsonUtils.getMetaJson(typeId, datasetId, repoUrl, serverUrlPrefix);
                return insertJsonStringToMongo(database, typeId, datasetId, outJson, true);
            } else {
                logger.debug("Document already exists");    //$NON-NLS-1$
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
            return insertJsonStringToMongo(database, collId, docId, inJson, true);
        } else {
            // check if the document already exists
            boolean isDocExist = isDocumentExist(database, collId, docId);
            if (!isDocExist) {
                // insert document into collection
                return insertJsonStringToMongo(database, collId, docId, inJson, true);
            } else {
                logger.debug("Document already exists");    //$NON-NLS-1$
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
            String outJson = ControllerJsonUtils.getCsvJson(typeId, datasetId, repoUrl);
            return insertCsvJsonToMongo(database, typeId, datasetId, outJson);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists.");  //$NON-NLS-1$   //$NON-NLS-2$
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String outJson = ControllerJsonUtils.getCsvJson(typeId, datasetId, repoUrl);
                return insertCsvJsonToMongo(database, typeId, datasetId, outJson);
            } else {
                logger.debug("Document already exists");    //$NON-NLS-1$
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
            String geoJson = ControllerJsonUtils.getGeoJson(typeId, datasetId, repoUrl);
            return insertGeoJsonToMongo(database, typeId, datasetId, geoJson);
        } else {
            logger.debug("Collection \"" + typeId + "\" already exists.");  //$NON-NLS-1$   //$NON-NLS-2$
            // check if the document is there
            boolean isDocExist = isDocumentExist(database, typeId, datasetId);
            if (!isDocExist) {
                // insert document into collection
                String geoJson = ControllerJsonUtils.getGeoJson(typeId, datasetId, repoUrl);
                return insertGeoJsonToMongo(database, typeId, datasetId, geoJson);
            } else {
                logger.debug("Document already exists");    //$NON-NLS-1$
                return false;
            }
        }
    }

    public static boolean insertDatasetToMongo(Dataset dataset, String mongoUrl, String mongoDbName){
        MongoDatabase database = getMongoDatabase(mongoUrl, mongoDbName);

        // convert Dataset obj to Mondo document
        Gson gson = new Gson();
        List<String> spaces = dataset.getSpaces();
        String datasetJsonStr = gson.toJson(dataset);
        System.out.println(datasetJsonStr);

        // check if the dataset id is already in the mongodb
        boolean isSpacesExist = isCollectionExist(database, ControllerFileUtils.DB_COLLECTION_SPACES);
        boolean isDatasetsExist = isCollectionExist(database, ControllerFileUtils.DB_COLLECTION_DATASETS);

        // create collection if collection is not there
        if (!isSpacesExist) {
            database.createCollection(ControllerFileUtils.DB_COLLECTION_SPACES, new CreateCollectionOptions().capped(false));
        }
        if (!isDatasetsExist) {
            database.createCollection(ControllerFileUtils.DB_COLLECTION_DATASETS, new CreateCollectionOptions().capped(false));
        }
        // insert document into collection
        boolean isDatasetInserted = false;
        isDatasetInserted = insertJsonStringToMongo(database, ControllerFileUtils.DB_COLLECTION_DATASETS, "", datasetJsonStr, false);

        return isDatasetInserted;
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

    // POJO method
    // check if collection exists, otherwise create one with a type id
    public static boolean isCollectionExist(MongoDatabase database, String collectionName) {
        boolean isCollExist = false;
        MongoIterable<String> collNames = database.listCollectionNames();

        for (String collName: collNames) {
            if (collName.equalsIgnoreCase(collectionName)) {
                isCollExist = true;
                return isCollExist;
            }
        }
        return isCollExist;
    }

    // check if the document with dataset name exist in the collection
    public static boolean isDocumentExist(MongoDatabase database, String collectionName, String docName ){
        boolean isDocExist = false;
        MongoCollection<Document> tmpCollection = database.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", docName);  //$NON-NLS-1$
        FindIterable existDoc = tmpCollection.find(query);

        if (existDoc.first() != null) {
            isDocExist = true;
            return isDocExist;
        }
        return isDocExist;
    }

    public static boolean insertGeoJsonToMongo(MongoDatabase database, String typeId, String datasetId, String inJson) {
        try {
            MongoCollection<Document> geoJsonColl = database.getCollection(typeId);
            BasicDBObject document = (BasicDBObject) JSON.parse(inJson);
            document.put("_id", datasetId); //$NON-NLS-1$
            geoJsonColl.insertOne(new Document(document));
            geoJsonColl.createIndex(new BasicDBObject("geometry","2dsphere"));  //$NON-NLS-1$ //$NON-NLS-2$
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }

    public static boolean insertCsvJsonToMongo(MongoDatabase database, String collId, String docId, String inJson) {
        try {
            MongoCollection<Document> csvJsonColl = database.getCollection(collId);
            BasicDBList docList = (BasicDBList) JSON.parse(inJson);
            Document document = new Document();
            document.put("_id", docId); //$NON-NLS-1$
            document.put("table", docList); //$NON-NLS-1$
            csvJsonColl.insertOne(document);
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }

    public static boolean insertJsonStringToMongo(MongoDatabase database, String collId, String docId, String inJson, boolean idIn) {
        try {
            MongoCollection<Document> collName = database.getCollection(collId);
            BasicDBObject docObj = (BasicDBObject) JSON.parse(inJson);
            if (idIn) {
                docObj.put("_id", docId);   //$NON-NLS-1$
            }
            collName.insertOne(new Document(docObj));
            FindIterable fItr = collName.find().sort(descending("_id")).limit(1);
            System.out.println(docObj.get("_id"));
        } catch (JSONException ex) {
            logger.error(ex);
            return false;
        }
        return true;
    }
}
