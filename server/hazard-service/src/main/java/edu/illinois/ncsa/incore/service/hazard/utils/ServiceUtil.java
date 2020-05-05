/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.utils;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.HazardConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ServiceUtil {

    private static final Logger logger = Logger.getLogger(ServiceUtil.class);

    /**
     * Utility for the hazard service to save files to the dataset repository
     *
     * @param datasetObject
     * @param creator
     * @param fileParts
     * @return
     * @throws IOException
     */
    public static String createDataset(JSONObject datasetObject, String creator,
                                       List<FormDataBodyPart> fileParts) throws IOException {
        // TODO cleanup duplicate code
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL") != null ?
            System.getenv("SERVICES_URL") : Config.getConfigProperties().getProperty("services.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.addTextBody(HazardConstants.DATASET_PARAMETER, datasetObject.toString());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        httpPost.setEntity(params.build());
        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JSONObject object = new JSONObject(responseStr);

            String datasetId = object.getString("id");
            requestUrl += "/" + datasetId + "/" + HazardConstants.DATASETS_FILES;

            params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for (FormDataBodyPart filePart : fileParts) {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) filePart.getEntity();
                params.addBinaryBody(HazardConstants.FILE_PARAMETER_, bodyPartEntity.getInputStream(), ContentType.DEFAULT_BINARY, filePart.getContentDisposition().getFileName());
            }

            // Attach file
            httpPost = new HttpPost(requestUrl);
            httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
            httpPost.setEntity(params.build());

            response = httpclient.execute(httpPost);
            responseStr = responseHandler.handleResponse(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return datasetId;
            }
        }

        return null;
    }

    /**
     * Utility for the hazard service to save files to the dataset repository
     *
     * @param datasetObject
     * @param creator
     * @param files
     * @return
     * @throws IOException
     */
    public static String createDataset(JSONObject datasetObject, String creator, File[] files) throws IOException {
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL") != null ?
            System.getenv("SERVICES_URL") : Config.getConfigProperties().getProperty("services.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.addTextBody(HazardConstants.DATASET_PARAMETER, datasetObject.toString());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        httpPost.setEntity(params.build());
        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JSONObject object = new JSONObject(responseStr);

            String datasetId = object.getString("id");
            requestUrl += "/" + datasetId + "/" + HazardConstants.DATASETS_FILES;

            params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for (File file : files) {
                params.addBinaryBody(HazardConstants.FILE_PARAMETER_, file);
            }

            // Attach file
            httpPost = new HttpPost(requestUrl);
            httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
            httpPost.setEntity(params.build());

            response = httpclient.execute(httpPost);
            responseStr = responseHandler.handleResponse(response);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return datasetId;
            }
        }

        return null;

    }

    public static JSONObject getDatasetJsonFromDataService(String datasetId, String creator) {
        JSONObject datasetJson = new JSONObject();

        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL") != null ?
            System.getenv("SERVICES_URL") : Config.getConfigProperties().getProperty("services.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        InputStream inputStream = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpclient = builder.build();

            String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT + "/" + datasetId;
            HttpGet httpGet = new HttpGet(requestUrl);
            httpGet.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");

            HttpResponse response = null;

            response = httpclient.execute(httpGet);
            inputStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String result = sb.toString();
            datasetJson = new JSONObject(result);

        } catch (IOException e) {
            // TODO add logging
            logger.error(e);
        }

        return datasetJson;
    }

    public static List<File> getFileDescriptorFileList(JSONObject datasetJson) {
        List outlist = new LinkedList<String>();
        String restStorageDir = System.getenv("DATA_REPO_DATA_DIR") != null ?
            System.getenv("DATA_REPO_DATA_DIR") : Config.getConfigProperties().getProperty("data.repo.data.dir");

        JSONArray fdList = (JSONArray) (datasetJson.get("fileDescriptors"));
        for (Object fd : fdList) {
            String filePath = restStorageDir + File.separator + (String) (((JSONObject) (fd)).get("dataURL"));
            // the following line is only for PC's testing, convert slash to file separator
//            filePath = filePath.replace('/', '\\');
            outlist.add(new File(filePath));
        }

        return outlist;
    }

    public static String collectShapfileInFolder(List<File> fileList, File tempFile) throws IOException {
        String outPath = null;
        String tempDir = tempFile.getPath();

        for (int i = 0; i < fileList.size(); i++) {
            File sourceFile = fileList.get(i);
            String fileName = FilenameUtils.getName(sourceFile.getName());
            String fileExt = FilenameUtils.getExtension(sourceFile.getName());
            File destFile = new File(tempDir + File.separator + fileName);

            if (fileExt.equalsIgnoreCase("shp")) {
                outPath = destFile.getPath();
            }

            org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
        }

        return outPath;
    }

    public static File getFileFromDataService(String datasetId, String creator, File incoreWorkDirectory) {
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL") != null ?
            System.getenv("SERVICES_URL") : Config.getConfigProperties().getProperty("services.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        InputStream inputStream = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpclient = builder.build();

            String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT + "/" + datasetId + "/blob";
            HttpGet httpGet = new HttpGet(requestUrl);
            httpGet.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");

            HttpResponse response = null;

            response = httpclient.execute(httpGet);
            inputStream = response.getEntity().getContent();
        } catch (IOException e) {
            // TODO add logging
            logger.error(e);
        }

        String filename = "files.zip";
        File file = new File(incoreWorkDirectory, filename);

        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))
        ) {

            int inByte;
            while ((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }

        } catch (IOException e) {

            logger.error(e);
        }

        return file;
    }

    public static String getDataServiceEndpoint() {
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL") != null ?
            System.getenv("SERVICES_URL") : Config.getConfigProperties().getProperty("services.url");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
    }

    public static String createDataset(String title, String creator, String description, String datasetType) throws IOException {
        String dataEndpoint = getDataServiceEndpoint();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HazardConstants.DATA_TYPE, datasetType);
        jsonObject.put(HazardConstants.TITLE, title);
        jsonObject.put(HazardConstants.SOURCE_DATASET, "");
        jsonObject.put(HazardConstants.FORMAT, HazardConstants.RASTER_FORMAT);
        jsonObject.put(HazardConstants.DESCRIPTION, description);

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.addTextBody(HazardConstants.DATASET_PARAMETER, jsonObject.toString());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        httpPost.setEntity(params.build());
        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JSONObject object = new JSONObject(responseStr);

            String datasetId = object.getString("id");
            return datasetId;
        }

        return null;
    }

    //TODO: Obsolete this and use createVisualizationDataset instead?
    public static String createRasterDataset(File rasterFile, String title, String creator,
                                             String description, String datasetType) throws IOException {
        String datasetId = createDataset(title, creator, description, datasetType);

        if (datasetId != null) {
            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            params.addBinaryBody(HazardConstants.FILE_PARAMETER_, rasterFile);

            attachFileToDataset(datasetId, creator, params);
        }

        return datasetId;
    }

    public static String createVisualizationDataset(List<File> vizFiles, String title, String creator,
                                                    String description, String datasetType) throws IOException {
        String datasetId = createDataset(title, creator, description, datasetType);

        if (datasetId != null) {
            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            for (File vizFile : vizFiles) {
                params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                params.addBinaryBody(HazardConstants.FILE_PARAMETER_, vizFile);
            }
            attachFileToDataset(datasetId, creator, params);
        }

        return datasetId;
    }

    public static String createRasterDataset(String filename, InputStream fis, String title, String creator,
                                             String description, String datasetType) throws IOException {
        String datasetId = createDataset(title, creator, description, datasetType);
        if (datasetId != null) {
            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            params.addBinaryBody(HazardConstants.FILE_PARAMETER_, fis, ContentType.DEFAULT_BINARY, filename);

            attachFileToDataset(datasetId, creator, params);
        }
        return datasetId;
    }

    public static void attachFileToDataset(String datasetId, String creator,
                                           MultipartEntityBuilder params) throws IOException {
        String dataEndpoint = getDataServiceEndpoint();
        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        requestUrl += "/" + datasetId + "/" + HazardConstants.DATASETS_FILES;

        // Attach file
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
        httpPost.setEntity(params.build());

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        HttpResponse response = httpclient.execute(httpPost);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = responseHandler.handleResponse(response);

        // This could be useful if there is a failure
        logger.debug("Attach file response " + responseStr);
    }

    public static File getWorkDirectory() {
        File incoreWorkDirectory = null;
        try {
            incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            return incoreWorkDirectory;
        } catch (IOException e) {
            logger.error("Error creating temporary directory.", e);
        }
        return incoreWorkDirectory;
    }

    public static File getCacheDirectory(String subdirectory) throws IOException {
        File cacheDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "incore-cache" + File.separator + subdirectory);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IOException("Could not create temp cache directory: " + cacheDir.getAbsolutePath());
            }
        }
        return cacheDir;
    }
}
