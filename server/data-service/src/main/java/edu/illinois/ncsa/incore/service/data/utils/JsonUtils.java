/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
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
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ywkim on 9/27/2017.
 */
public class JsonUtils {
    public static final Logger logger = Logger.getLogger(JsonUtils.class);
    // create json from the csv file
    public static String getCsvJson(String typeId, String datasetId, String repoUrl) {
        File dataset = null;
        String combinedId = typeId + "/" + datasetId + "/converted/";   //$NON-NLS-1$
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
            String tempDir = Files.createTempDirectory("repo_download_").toString();    //$NON-NLS-1$
            HttpDownloader.downloadFile(datasetUrl + datasetId + "." + FileUtils.EXTENSION_META, tempDir);
            fileName = tempDir + File.separator + datasetId + "." + FileUtils.EXTENSION_META;
            if (fileName.length() > 0) {
                dataset = new File(fileName);
                outJson = MvzLoader.formatMetadataAsJson(dataset, combinedId);
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
            fileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP, repoUrl);
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

    public static String getJsonByDatasetId(String datasetId) {
        List<String> resHref = FileUtils.getDirectoryContent(FileUtils.REPO_PROP_URL, "");

        for (String typeUrl: resHref) {
            String fileDirUrl = FileUtils.REPO_DS_URL + typeUrl + "/" + datasetId + "/converted/";    //$NON-NLS-1$
            List<String> fileHref = FileUtils.getDirectoryContent(fileDirUrl, "");
            if (fileHref.size() > 1) {
                for (String fileNameInDir : fileHref) {
                    String fileExtStr = FilenameUtils.getExtension(fileNameInDir);
                    String fileName = FilenameUtils.getName(fileNameInDir);
                    String outJson = "";    //$NON-NLS-1$
                    try {
                        if (fileExtStr.equals(FileUtils.EXTENSION_SHP)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";  //$NON-NLS-1$
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_SHP, FileUtils.REPO_DS_URL);
                            File dataset = new File(localFileName);
                            outJson = formatDatasetAsGeoJson(dataset);
                            return outJson;
                        } else if (fileExtStr.equals(FileUtils.EXTENSION_CSV)) {
                            String combinedId = typeUrl + "/" + datasetId + "/converted/";  //$NON-NLS-1$
                            String localFileName = FileUtils.loadFileNameFromRepository(combinedId, FileUtils.EXTENSION_CSV, FileUtils.REPO_DS_URL);
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
