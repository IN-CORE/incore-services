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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.HazardConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServiceUtil {

    private static final Logger logger = Logger.getLogger(ServiceUtil.class);

    /**
     * util for creating a tornado dataset based on input json
     *
     * @param datasetObject
     * @param creator
     * @return
     * @throws IOException
     */
    public static String createDataset(JSONObject datasetObject, String creator)
        throws IOException {
        // TODO cleanup duplicate code
        // CMN: we could go through Kong, but then we would need a token
        String dataEndpoint = createDataEndpoint();

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

        JSONObject object = new JSONObject(responseStr);
        String datasetId = object.getString("id");

        return datasetId;
    }

    /**
     * util for attaching tornado shapefile or zipfile to tornado dataset created
     *
     * @param datasetId
     * @param creator
     * @param fileParts
     * @return
     * @throws IOException
     */
    public static int attachFileToTornadoDataset(String datasetId, String creator, List<FormDataBodyPart> fileParts) throws IOException {
        String dataEndpoint = createDataEndpoint();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT;
        requestUrl += "/" + datasetId + "/" + HazardConstants.DATASETS_FILES;

        MultipartEntityBuilder params = MultipartEntityBuilder.create();
        params.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (FormDataBodyPart filePart : fileParts) {
            BodyPartEntity bodyPartEntity = (BodyPartEntity) filePart.getEntity();
            params.addBinaryBody(HazardConstants.FILE_PARAMETER_, bodyPartEntity.getInputStream(), ContentType.DEFAULT_BINARY,
                filePart.getContentDisposition().getFileName());
        }

        // Attach file
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creator + "\"}");
        httpPost.setEntity(params.build());

        HttpResponse response = null;
        String responseStr = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);

//        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//            return datasetId;
//        }

        return response.getStatusLine().getStatusCode();
    }

    /**
     * utility for creating data endpoint string
     *
     * @return
     */
    public static String createDataEndpoint() {
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("SERVICES_URL");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
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
        String dataEndpointProp = System.getenv("SERVICES_URL");
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
        String dataEndpointProp = System.getenv("SERVICES_URL");
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
                logger.debug("IO Exception", e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.debug("IO Exception", e);
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
        String restStorageDir = System.getenv("DATA_REPO_DATA_DIR");

        JSONArray fdList = (JSONArray) (datasetJson.get("fileDescriptors"));
        for (Object fd : fdList) {
            String filePath = restStorageDir + File.separator + ((JSONObject) (fd)).get("dataURL");
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
        String dataEndpointProp = System.getenv("SERVICES_URL");
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
        String dataEndpointProp = System.getenv("SERVICES_URL");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
    }

    public static String submitCreateEarthquakeJob(String workflowId, String creator, String title, String description, String eqJson) {
        JSONObject submission = new JSONObject();
        submission.put("workflowId", workflowId);
        submission.put("title", title);
        // TODO replace this with a call to datawolf to fetch the creator ID associated with the creator
        submission.put("creatorId", "6140eada-2784-44bd-b40e-31096392163e");
        submission.put("description", description);

        File incoreWorkDirectory = null;
        try {
            incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            File eqJsonFile = new File(incoreWorkDirectory, "eq-model.json");

            FileOutputStream fileOutputStream = new FileOutputStream(eqJsonFile);
            byte[] contentBytes = eqJson.getBytes();
            fileOutputStream.write(contentBytes);
            fileOutputStream.flush();
            fileOutputStream.close();

            String datasetId = uploadWorkflowDataset("eq-model", "eq model json", eqJsonFile, creator);
            JSONObject datasets = new JSONObject();
            datasets.put("9f80ea38-14b1-46b0-b943-faa5b24914ed", datasetId);
            submission.put("datasets", datasets);

        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return submitWorkflowJob(submission);
    }

    public static String uploadWorkflowDataset(String title, String description, File file, String creator) {
        String requestUrl = getDataWolfService() + "datasets";
        HttpPost post = new HttpPost(requestUrl);
        HttpClientBuilder builder = HttpClientBuilder.create();

        try {
            HttpClient client = builder.build();
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

            entityBuilder.addBinaryBody("uploadedFile", file);
            entityBuilder.addTextBody("useremail", creator);
            entityBuilder.addTextBody("description", description);
            entityBuilder.addTextBody("title", title);

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = null;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            }

        } catch (ClientProtocolException e) {
            logger.debug("Client Protocol Exception", e);
        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return null;
    }

    public static String submitWorkflowJob(JSONObject submission) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = getDataWolfService() + "executions";
        HttpPost httpPost = new HttpPost(requestUrl);

        String json = null;
        try {
            json = submission.toString();

            StringEntity input = new StringEntity(json);
            input.setContentType("application/json");

            httpPost.setEntity(input);

            HttpResponse response = null;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = null;

            response = httpclient.execute(httpPost);
            responseStr = responseHandler.handleResponse(response);

            logger.debug("Job submission HTTP response: " + response);
            logger.debug("Execution ID: " + responseStr);
            return responseStr;
        } catch (JsonProcessingException e) {
            logger.debug("Json Processing Exception", e);
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unsupported Encoding Exception", e);
        } catch (ClientProtocolException e) {
            logger.debug("Client Protocol Exception", e);
        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return null;
    }

    public static Map<String, String> getWorkflowJobStatus(String executionId) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = getDataWolfService() + "executions" + "/" + executionId + "/" + "state";
        HttpGet httpGet = new HttpGet(requestUrl);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = null;

            HttpResponse response = httpclient.execute(httpGet);
            responseStr = responseHandler.handleResponse(response);

            logger.debug("Job status HTTP response: " + response);
            logger.debug("Job status: " + responseStr);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> jobStatusMap = mapper.readValue(responseStr, Map.class);
            return jobStatusMap;
        } catch (JsonProcessingException e) {
            logger.debug("Json Processing Exception", e);
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unsupported Encoding Exception", e);
        } catch (ClientProtocolException e) {
            logger.debug("Client Protocol Exception", e);
        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return null;
    }

    public static Map<String, String> getWorkflowOutputs(String executionId, List<String> datasetIds) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = getDataWolfService() + "executions" + "/" + executionId;
        HttpGet httpGet = new HttpGet(requestUrl);
        Map<String, String> datasetMap = new HashMap<>();
        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = null;

            HttpResponse response = httpclient.execute(httpGet);
            responseStr = responseHandler.handleResponse(response);

            logger.debug("Get execution HTTP response: " + response);
            logger.debug("Get execution json: " + responseStr);

            JSONObject execution = new JSONObject(responseStr);
            JSONObject datasetObj = (JSONObject) execution.get("datasets");

            for (String datasetId : datasetIds) {
                String outputDatasetId = datasetObj.getString(datasetId);
                datasetMap.put(datasetId, outputDatasetId);
            }
        } catch (JsonProcessingException e) {
            logger.debug("Json Processing Exception", e);
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unsupported Encoding Exception", e);
        } catch (ClientProtocolException e) {
            logger.debug("Client Protocol Exception", e);
        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return datasetMap;
    }

    public static List<File> getWorkflowDatasetFiles(String datasetId) {
        List<File> files = new LinkedList<>();
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = getDataWolfService() + "datasets" + "/" + datasetId;
        HttpGet httpGet = new HttpGet(requestUrl);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        HttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            responseStr = responseHandler.handleResponse(response);

            logger.debug("Job dataset HTTP response: " + response);
            logger.debug("Job dataset object: " + responseStr);

            JSONObject dataset = new JSONObject(responseStr);
            JSONArray fileDescriptors = (JSONArray) dataset.get("fileDescriptors");
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");

            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            // Get dataset files - these needs to be generalized
            for (int index = 0; index < fileDescriptors.length(); index++) {
                JSONObject fileDescriptor = fileDescriptors.getJSONObject(index);
                String descriptorId = fileDescriptor.getString("id");
                String filename = fileDescriptor.getString("filename");
                requestUrl = getDataWolfService() + "datasets" + "/" + datasetId + "/" + descriptorId + "/" + "file";

                HttpGet httpGetFile = new HttpGet(requestUrl);
                HttpResponse fileResponse = httpclient.execute(httpGetFile);

                BufferedInputStream is = new BufferedInputStream(fileResponse.getEntity().getContent());
                File file = new File(incoreWorkDirectory, filename);

                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                int bytes;
                while ((bytes = is.read()) != -1) {
                    os.write(bytes);
                }

                is.close();
                os.close();

                files.add(file);
            }

        } catch (IOException e) {
            logger.debug("IO Exception", e);
        }

        return files;
    }

    public static String createDataset(String title, String creator, String description, String datasetType,
                                       String format) throws IOException {
        String dataEndpoint = getDataServiceEndpoint();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HazardConstants.DATA_TYPE, datasetType);
        jsonObject.put(HazardConstants.TITLE, title);
        jsonObject.put(HazardConstants.SOURCE_DATASET, "");
        jsonObject.put(HazardConstants.FORMAT, format);
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
        String datasetId = createDataset(title, creator, description, datasetType, HazardConstants.RASTER_FORMAT);

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
        String datasetId = createDataset(title, creator, description, datasetType, HazardConstants.SHAPEFILE_FORMAT);

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
        String datasetId = createDataset(title, creator, description, datasetType, HazardConstants.RASTER_FORMAT);
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

    public static String deleteDataset(String datasetId, String user) {
        String dataEndpoint = getDataServiceEndpoint();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = dataEndpoint + HazardConstants.DATASETS_ENDPOINT + "/" + datasetId;

        HttpDelete httpDel = new HttpDelete(requestUrl);
        httpDel.setHeader(HazardConstants.X_AUTH_USERINFO, "{\"preferred_username\": \"" + user + "\"}");

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        try {
            response = httpclient.execute(httpDel);
            responseStr = responseHandler.handleResponse(response);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                JSONObject object = new JSONObject(responseStr);

                String retDatasetId = object.getString("id");
                return retDatasetId;
            } else {
                logger.error("Deleting dataset " + datasetId + " failed with status code " + statusCode);
            }
        } catch (Exception ex) {
            logger.error("Error deleting the dataset " + datasetId, ex);
        }
        return null;
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

    /**
     * DataWolf Service Endpoint
     *
     * @return
     */
    public static String getDataWolfService() {
        String dwurl = System.getenv("DATAWOLF_URL");
        if (dwurl != null && !dwurl.isEmpty()) {
            if (!dwurl.endsWith("/")) {
                dwurl += "/";
            }
        }

        return dwurl;
    }

}
