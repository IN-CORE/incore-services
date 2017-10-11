package edu.illinois.ncsa.incore.service.data.controllers.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.model.MvzLoader;
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ywkim on 9/27/2017.
 */
public class ControllerJsonUtils {
    public static final Logger logger = Logger.getLogger(ControllerJsonUtils.class);
    // create json from the csv file
    public static String getCsvJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
        String outJson = "";
        String fileName = "";
        try{
            fileName = ControllerFileUtils.loadFileNameFromRepository(combinedId, ControllerFileUtils.EXTENSION_CSV, repoUrl);
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
            String tempDir = Files.createTempDirectory("repo_download_").toString();    //$NON-NLS-1$
            HttpDownloader.downloadFile(datasetUrl + datasetId + "." + ControllerFileUtils.EXTENSION_META, tempDir);
            fileName = tempDir + File.separator + datasetId + "." + ControllerFileUtils.EXTENSION_META;
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = MvzLoader.formatMetadataAsJson(dataset, combinedId, serverUrlPrefix);
            }
        }catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";   //$NON-NLS-1$
        }
        return outJson;
    }

    // create geoJson from the shapefile url
    public static String getGeoJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
        String outJson = "";
        String fileName = "";
        try{
            fileName = ControllerFileUtils.loadFileNameFromRepository(combinedId, ControllerFileUtils.EXTENSION_SHP, repoUrl);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatDatasetAsGeoJson(dataset);
            }
        }catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";   //$NON-NLS-1$
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

    public static String getJsonByDatasetIdFromMongo(String datasetId, String mongoUrl, String geoDbName){
        MongoDatabase database = ControllerMongoUtils.getMongoDatabase(mongoUrl, geoDbName);
        MongoIterable<String> collNames = database.listCollectionNames();
        org.bson.Document result = new Document();
        String outJson = "";    //$NON-NLS-1$

        for (String collectionName: collNames) {
            MongoCollection<Document> tmpCollection = database.getCollection(collectionName);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", datasetId);    //$NON-NLS-1$
            FindIterable existDoc = tmpCollection.find(query);

            if (existDoc.first() != null) {
                result = (Document) existDoc.first();
                JsonWriterSettings writerSettings = new JsonWriterSettings(JsonMode.SHELL, true);
                outJson = result.toJson(writerSettings);
                return outJson;
            }
        }

        return outJson;
    }

    public static String extractValueFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        if (jsonObj.has(inId)) {
            Object output = jsonObj.get(inId);
            return output.toString();
        } else {
            return "";  //$NON-NLS-1$
        }
    }

    public static List<String> extractValueListFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        List<String> outList = new LinkedList<String>();
        if (jsonObj.has(inId)) {
            try {
                JSONArray inArray = (JSONArray) jsonObj.get(inId);
                for (Object jObj: inArray) {
                    outList.add(jObj.toString());
                }
                return outList;
            } catch (JSONException e) {
                return outList;
            }
        } else {
            return outList;
        }
    }

    public static String formatDatasetAsGeoJson(File shapefile) throws IOException {
        //TODO: this should return the data in geoJSON format
        String geoJsonStr;

        ShapefileDataStore store = new ShapefileDataStore(shapefile.toURI().toURL());
        SimpleFeatureSource source = store.getFeatureSource();
        SimpleFeatureCollection featureCollection = source.getFeatures();
        FeatureJSON fjson = new FeatureJSON();

        try (StringWriter writer = new StringWriter()) {
            fjson.writeFeatureCollection(featureCollection, writer);
            geoJsonStr = writer.toString();
        }

        ControllerFileUtils.deleteTmpDir(shapefile, ControllerFileUtils.EXTENSIONS_SHAPEFILES);

        return geoJsonStr;
    }

    private static String formatCsvAsJson(File inCsv, String inId) throws IOException {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(csvSchema).readValues(inCsv);

        List<Map<?, ?>> data =  mappingIterator.readAll();
        ObjectMapper mapper = new ObjectMapper();
        String outStr = mapper.writeValueAsString(data);

        ControllerFileUtils.deleteTmpDir(inCsv, ControllerFileUtils.EXTENSION_CSV);

        return outStr;
    }

    public static String getJsonByDatasetId(String datasetId) {
        List<String> resHref = ControllerFileUtils.getDirectoryContent(ControllerFileUtils.REPO_PROP_URL, "");

        for (String typeUrl: resHref) {
            String fileDirUrl = ControllerFileUtils.REPO_DS_URL + typeUrl + "/" + datasetId + "/converted/";    //$NON-NLS-1$
            List<String> fileHref = ControllerFileUtils.getDirectoryContent(fileDirUrl, "");
            if (fileHref.size() > 1) {
                for (String fileNameInDir : fileHref) {
                    String fileExtStr = FilenameUtils.getExtension(fileNameInDir);
                    String fileName = FilenameUtils.getName(fileNameInDir);
                    String outJson = "";    //$NON-NLS-1$
                    try {
                        if (fileExtStr.equals(ControllerFileUtils.EXTENSION_SHP)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";  //$NON-NLS-1$
                            String localFileName = ControllerFileUtils.loadFileNameFromRepository(combinedId, ControllerFileUtils.EXTENSION_SHP, ControllerFileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatDatasetAsGeoJson(dataset);
                            return outJson;
                        } else if (fileExtStr.equals(ControllerFileUtils.EXTENSION_CSV)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";  //$NON-NLS-1$
                            String localFileName = ControllerFileUtils.loadFileNameFromRepository(combinedId, ControllerFileUtils.EXTENSION_CSV, ControllerFileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatCsvAsJson(dataset, datasetId);
                            return outJson;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";  //$NON-NLS-1$
                    }
                }
            }

        }
        return "";  //$NON-NLS-1$
    }
}
