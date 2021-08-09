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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ywkim on 9/27/2017.
 */
public class DataJsonUtils {
    public static final Logger logger = Logger.getLogger(DataJsonUtils.class);

    // create json from the csv file
    public static String getCsvJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";
        String outJson = "";
        String fileName = "";
        try {
            fileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_CSV, repoUrl);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatCsvAsJson(dataset, combinedId);
            }
        } catch (IOException e) {
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
        try {
            String tempDir = Files.createTempDirectory("repo_download_").toString();
            HttpDownloader.downloadFile(datasetUrl + datasetId + "." + FileUtils.EXTENSION_META, tempDir);
            fileName = tempDir + File.separator + datasetId + "." + FileUtils.EXTENSION_META;
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = MvzLoader.formatMetadataAsJson(dataset, combinedId);
            }
        } catch (IOException e) {
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
        try {
            fileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP, repoUrl);
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = formatDatasetAsGeoJson(dataset);
            }
        } catch (IOException e) {
            e.printStackTrace();
//            outJson = "{\"error:\" + \"" + e.getLocalizedMessage() + "\"}";
        }
        return outJson;
    }

    public static HashMap<String, Object> extractMapFromJsonString(String inJson) {
        try {
            return new ObjectMapper().readValue(inJson, HashMap.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static List<String> extractValueListFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        List<String> outList = new LinkedList<String>();
        if (jsonObj.has(inId)) {
            try {
                JSONArray inArray = (JSONArray) jsonObj.get(inId);
                for (Object jObj : inArray) {
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

    public static NetworkDataset createNetworkDataset(String inJson) {
        NetworkDataset networkDataset = new NetworkDataset();

        String componentStr = "";
        String nodeStr = "";
        String linkStr = "";
        String graphStr = "";
        String linkType = "";
        String nodeType = "";
        String graphType = "";

        NetworkData link = new NetworkData();
        NetworkData node = new NetworkData();
        NetworkData graph = new NetworkData();

        componentStr = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_COMPONENT, inJson);
        linkStr = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_LINK, componentStr);
        linkType = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_LINK_TYPE, linkStr);
        nodeStr = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_NODE, componentStr);
        nodeType = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_NODE_TYPE, nodeStr);
        graphStr = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_GRAPH, componentStr);
        graphType = JsonUtils.extractValueFromJsonString(FileUtils.NETWORK_GRAPH_TYPE, graphStr);

        link.setNetworkType(linkType);
        node.setNetworkType(nodeType);
        graph.setNetworkType(graphType);
        networkDataset.setLink(link);
        networkDataset.setNode(node);
        networkDataset.setGraph(graph);

        return networkDataset;
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

        FileUtils.deleteTmpDir(shapefile, FileUtils.EXTENSIONS_SHAPEFILES);

        return geoJsonStr;
    }

    private static String formatCsvAsJson(File inCsv, String inId) throws IOException {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(csvSchema).readValues(inCsv);

        List<Map<?, ?>> data = mappingIterator.readAll();
        ObjectMapper mapper = new ObjectMapper();
        String outStr = mapper.writeValueAsString(data);

        FileUtils.deleteTmpDir(inCsv, FileUtils.EXTENSION_CSV);

        return outStr;
    }

    public static boolean isDatasetParameterValid(String inJson) {
        Field[] allFields = Dataset.class.getDeclaredFields();
        List<String> datasetParams = Arrays.stream(allFields).map(Field::getName).collect(Collectors.toList());

        Object json = null;
        Set<String> jsonKeys = null;
        try {
            json = new JSONObject(inJson);
            jsonKeys = ((JSONObject) json).keySet();
        } catch (JSONException ex) {
            try {
                json = new JSONArray(inJson);
                jsonKeys = ((JSONObject) json).keySet();
            } catch (JSONException ex1) {
                return false;
            }
        }
        return jsonKeys.stream().allMatch(it -> datasetParams.contains(it));
    }

    public static String getJsonByDatasetId(String datasetId) {
        List<String> resHref = FileUtils.getDirectoryContent(FileUtils.REPO_PROP_URL, "");

        for (String typeUrl : resHref) {
            String fileDirUrl = FileUtils.REPO_DS_URL + typeUrl + "/" + datasetId + "/converted/";
            List<String> fileHref = FileUtils.getDirectoryContent(fileDirUrl, "");
            if (fileHref.size() > 1) {
                for (String fileNameInDir : fileHref) {
                    String fileExtStr = FilenameUtils.getExtension(fileNameInDir);
                    String fileName = FilenameUtils.getName(fileNameInDir);
                    String outJson = "";
                    try {
                        if (fileExtStr.equals(FileUtils.EXTENSION_SHP)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP,
                                FileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatDatasetAsGeoJson(dataset);
                            return outJson;
                        } else if (fileExtStr.equals(FileUtils.EXTENSION_CSV)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_CSV,
                                FileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatCsvAsJson(dataset, datasetId);
                            return outJson;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            }

        }
        return "";
    }

    public static String parseUserName(String userInfo) {
        String userName = null;
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject userInfoJson = (org.json.simple.JSONObject) parser.parse(userInfo);
            userName = (String) userInfoJson.get("preferred_username");

        } catch (ParseException e) {
            logger.error("Unable to parse userInfo", e);
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to parse userInfo");
        }

        return userName;
    }

    public static JSONObject createUserStatusJson(String userInfo, IRepository repository, String keyDatabase) throws ParseException {
        List<Dataset> datasets = null;
        String userName = parseUserName(userInfo);

        if (userName == null) {
            logger.error("Error finding username");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the username");
        }

        if (keyDatabase.equalsIgnoreCase("hazard")) {
            datasets = repository.getDatasetByCreator(userName, true);
        } else if (keyDatabase.equalsIgnoreCase("dataset")) {
            datasets = repository.getDatasetByCreator(userName, false);
        }

        if (datasets == null) {
            logger.error("Error finding dataset");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any datasets");
        }

        int total_num_dataset = datasets.size();
        int total_file_size = 0;

        // add the file size of all files in fileDescriptors
        for (Dataset dataset : datasets) {
            List<FileDescriptor> fds = dataset.getFileDescriptors();
            for (FileDescriptor fd : fds) {
                total_file_size += fd.getSize();
            }
        }

        String out_file_size;

        double size_kb = total_file_size / 1024;
        double size_mb = size_kb / 1024;
        double size_gb = size_mb / 1024;

        // round values
        size_kb = Math.round(size_kb * 100.0) / 100.0;
        size_mb = Math.round(size_mb * 100.0) / 100.0;
        size_gb = Math.round(size_gb * 100.0) / 100.0;

        if (size_gb >= 1) {
            out_file_size = size_gb + " GB";
        } else if (size_mb >= 1) {
            out_file_size = size_mb + " MB";
        } else {
            out_file_size = size_kb + " KB";
        }

        JSONObject outJson = new JSONObject();
        outJson.put("user", userName);
        outJson.put("total_number_of_datasets", total_num_dataset);
        outJson.put("total_file_size", out_file_size);
        outJson.put("total_file_size_byte", total_file_size);

        return outJson;
    }

    public static int getNumHazardFromJson(String inJsonStr) {
        int hazardNum = 0;
        JSONArray jsonArray = new JSONArray(inJsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            int tmpNum = jsonObject.getInt("total_number_of_hazard");
            hazardNum = hazardNum + tmpNum;
        }

        return hazardNum;
    }
}
