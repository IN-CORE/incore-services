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
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.MvzLoader;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
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
        try{
            fileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_CSV, repoUrl);
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
            HttpDownloader.downloadFile(datasetUrl + datasetId + "." + FileUtils.EXTENSION_META, tempDir);
            fileName = tempDir + File.separator + datasetId + "." + FileUtils.EXTENSION_META;
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = MvzLoader.formatMetadataAsJson(dataset, combinedId);
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
            fileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP, repoUrl);
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



    public static HashMap<String, Object> extractMapFromJsonString(String inJson){
        try {
            return new ObjectMapper().readValue(inJson, HashMap.class);
        } catch (IOException e){
            return null;
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

        FileUtils.deleteTmpDir(shapefile, FileUtils.EXTENSIONS_SHAPEFILES);

        return geoJsonStr;
    }

    private static String formatCsvAsJson(File inCsv, String inId) throws IOException {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(csvSchema).readValues(inCsv);

        List<Map<?, ?>> data =  mappingIterator.readAll();
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

        for (String typeUrl: resHref) {
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
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP, FileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatDatasetAsGeoJson(dataset);
                            return outJson;
                        } else if (fileExtStr.equals(FileUtils.EXTENSION_CSV)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_CSV, FileUtils.REPO_DS_URL);
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
}
