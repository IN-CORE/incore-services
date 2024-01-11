package edu.illinois.ncsa.incore.service.data.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GeoserverRestApi {
    public static final String DEFAULT_CRS = "EPSG:4326";
    public static final String GEOSERVER_URL = System.getenv("GEOSERVER_URL");
    public static final String GEOSERVER_USER = System.getenv("GEOSERVER_USER");
    public static final String GEOSERVER_PW = System.getenv("GEOSERVER_PW");
    public static final String GEOSERVER_WORKSPACE = System.getenv("GEOSERVER_WORKSPACE");
    private static final Logger logger = Logger.getLogger(GeoserverUtils.class);
    private final String geoserverUrl;
    private final String username;
    private final String password;

    /**
     * create Geoserver rest api object
     *
     */
    public GeoserverRestApi() {
        String geoserverUrl = GEOSERVER_URL;
        String username = GEOSERVER_USER;
        String password = GEOSERVER_PW;

        // remove the slash if it is the last character of url string
        if (geoserverUrl!=null && geoserverUrl.endsWith("/")) {
            geoserverUrl = geoserverUrl.substring(0, geoserverUrl.length() - 1);
        }
        this.geoserverUrl = geoserverUrl;
        this.username = username;
        this.password = password;

        // check if the URL is in correct form
        URL testUrl = null;
        try {
            testUrl = new URL(geoserverUrl);
        } catch (MalformedURLException e) {
            logger.error("URL is not correct: Geoserver connection will not work correctly" , e);
        }
    }

    /**
     * create Geoserver rest api object
     *
     * @return
     */
    public static GeoserverRestApi createGeoserverApi() {
        return new GeoserverRestApi();
    }

    /**
     * post xml to geoserver
     *
     * @param apiUrl
     * @param inData
     * @param contType
     * @return
     */
    public String postXmlToGeoserver(String apiUrl, String inData, String contType) {
        try {
            HttpURLConnection connection = createHttpConnection(apiUrl, contType);
            // create request body using input xml
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = inData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            connection.disconnect();

            return "Response Code: " + responseCode;
        } catch (Exception e) {
            logger.error("Failed to POST to geoserver rest api", e);
            return null;
        }
    }

    /**
     * post file stream to geoserver
     *
     * @param apiUrl
     * @param inData
     * @param contType
     * @return
     */
    public boolean postFileToGeoserver(String apiUrl, File inData, String contType) {
        try {
            HttpURLConnection connection = createHttpConnection(apiUrl, contType);

            // Write Shapefile data to request body
            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                 FileInputStream fis = new FileInputStream(inData)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = connection.getResponseCode();
            boolean published = false;
            if (responseCode == 201 || responseCode == 200 || responseCode == 409) {
                published = true;
            }

            connection.disconnect();
            if (contType.equalsIgnoreCase("zip")) {
                logger.info("Successfully posted shapefile");
            } else if (contType.equalsIgnoreCase("gpkg")) {
                logger.info("Successfully posted geopackage");
            } else {
                logger.info("Successfully posted geotiff");
            }
            return published;
        } catch (Exception e) {
            logger.error("Failed to POST to geoserver rest api", e);
            System.out.println(e);
            return false;
        }
    }

    /**
     * upload file to geoserver
     *
     * @param store
     * @param inFile
     * @param inExt
     * @return
     */
    public boolean uploadToGeoserver(String store, File inFile, String inExt) {
        String fileName = FilenameUtils.getBaseName(inFile.getName());

        // check if workspace exists
        boolean created = createWorkspace(this.geoserverUrl, GEOSERVER_WORKSPACE);
        boolean published = false;

        if (inExt.equalsIgnoreCase("shp")) {
            String restUrl = this.geoserverUrl + "/rest/workspaces/" + GEOSERVER_WORKSPACE + "/datastores/" + store + "/file.shp";
            published = this.postFileToGeoserver(restUrl, inFile, "zip");
        } else if (inExt.equalsIgnoreCase("gpkg")) {
            // geopackage should have different steps due to its renaming convention in IN-CORE
            // since IN-CORE uses the dataset id as a file name in geoserver, the geopackage name needs to be renamed to dataset id
            // to rename, the POST action should create datastore first with the geopackage data,
            // then create layer with xml to control the name to be changed to dataset id.
            // if you don't need to rename, set renameData boolean in following line to false
            Boolean renameData = true;
            if (renameData) {
                // create datastore then create layer
                try {
                    String restUrl = this.geoserverUrl + "/rest";
                    String fileNameNoExt = fileName.split("\\.")[0];
                    int datastoreResponse = createDatastore(restUrl, GEOSERVER_WORKSPACE, store, inFile.getAbsolutePath(), "geopackage");
                    int layerResponse = createLayer(restUrl, GEOSERVER_WORKSPACE, store, store, fileNameNoExt);
                    if (datastoreResponse == 201 && layerResponse == 201) {
                        published = true;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else{
                String restUrl = this.geoserverUrl + "/rest/workspaces/" + GEOSERVER_WORKSPACE + "/datastores/" + store +
                    "/file.gpkg?configure=all&name=" + store;
                published = this.postFileToGeoserver(restUrl, inFile, "gpkg");
            }
        } else if (inExt.equalsIgnoreCase("tif")) {
            String restUrl = this.geoserverUrl + "/rest/workspaces/" + GEOSERVER_WORKSPACE + "/coveragestores/" + store + "/file.geotiff";
            published = this.postFileToGeoserver(restUrl, inFile, "tif");
        }

        return published;
    }

    /**
     *  XML template for feature
     *
     */
    public final String LAYER_XML_TEMPLATE = "<featureType>\n" +
        "  <name>{ft_name}</name>\n" +
        "  <title>{ft_title}</title>\n" +
        "  <nativeName>{ft_native_name}</nativeName>\n" +
        "</featureType>";

    /**
     * create workspace
     *
     * @param geoserverUrl
     * @param workspace
     * @return
     */
    public boolean createWorkspace(String geoserverUrl, String workspace) {
        final String restUrl = geoserverUrl + "/rest/workspaces";
        final String wsXml = "<workspace><name>" + workspace + "</name></workspace>";
        final String result = postXmlToGeoserver(restUrl, wsXml, "xml");

        return result != null;
    }

    /**
     * Create datastore in geoserver
     *
     * @param baseUrl
     * @param workspaceName
     * @param datastoreName
     * @param fileName
     * @param fileFormat
     * @throws IOException
     */
    public int createDatastore(String baseUrl, String workspaceName, String datastoreName,
                                        String fileName, String fileFormat) throws IOException {
        String formatUrl = "/file.shp";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/zip");

        if ("shapefile".equals(fileFormat)) {
            formatUrl = "/file.shp";
            headers.put("Content-Type", "application/zip");
        } else if ("geopackage".equals(fileFormat)) {
            formatUrl = "/file.gpkg";
            headers.put("Content-Type", "application/x-sqlite3");
        }

        String datastoreUrl = baseUrl + "/workspaces/" + workspaceName + "/datastores/"
            + datastoreName + formatUrl;

        Map<String, String> params = new HashMap<>();
        params.put("configure", "none");
        params.put("update", "overwrite");

        try (FileInputStream fileInputStream = new FileInputStream(new File(fileName))) {
            byte[] fileData = fileInputStream.readAllBytes();
            return sendHttpRequest(datastoreUrl, headers, fileData, params, "PUT");
        }
    }

    /**
     * Create layer after datastore created, this should be performed after createDatastore
     *
     * @param baseUrl
     * @param workspaceName
     * @param datastoreName
     * @param layerName
     * @param nativeName
     * @throws IOException
     */
    public int createLayer(String baseUrl, String workspaceName, String datastoreName,
                                    String layerName, String nativeName) throws IOException {
        String layerUrl = baseUrl + "/workspaces/" + workspaceName + "/datastores/" + datastoreName + "/featuretypes";
        String layerXml = LAYER_XML_TEMPLATE.replace("{ft_name}", layerName)
            .replace("{ft_title}", layerName)
            .replace("{ft_native_name}", nativeName);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/xml");

        int responseCode = sendHttpRequest(layerUrl, headers, layerXml.getBytes(), null, "POST");

        return responseCode;
    }

    /**
     * Send http request to geoserver
     *
     * @param url
     * @param headers
     * @param data
     * @param params
     * @param method
     * @throws IOException
     */
    public int sendHttpRequest(String url, Map<String, String> headers, byte[] data,
                                        Map<String, String> params, String method) throws IOException {
        HttpURLConnection connection = createHttpConnection(url, method, headers);

        if (params != null && !params.isEmpty()) {
            url += getQueryString(params);
        }

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(data);
        }

        int responseCode = connection.getResponseCode();

        return responseCode;
    }

    public HttpURLConnection createHttpConnection(String apiUrl, String method, Map<String, String> headers)
        throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set authentication and headers
        String credentials = GEOSERVER_USER + ":" + GEOSERVER_PW;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        // Set request method and content type
        connection.setRequestMethod(method);
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * create http connection to geoserver rest api
     *
     * @param apiUrl
     * @param contType
     * @return
     * @throws IOException
     */
    public HttpURLConnection createHttpConnection(String apiUrl, String contType) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // create connection with authentication and post request
        String credentials = GEOSERVER_USER + ":" + GEOSERVER_PW;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        if (contType.equalsIgnoreCase("xml")) {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setDoOutput(true);
        } else if (contType.equalsIgnoreCase("zip")) {
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/zip");
            connection.setDoOutput(true);
        } else if (contType.equalsIgnoreCase("gpkg")) {
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/x-sqlite3");
            connection.setDoOutput(true);
        } else if (contType.equalsIgnoreCase("tif")) {
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "image/tiff");
            connection.setDoOutput(true);
        } else if (contType.equalsIgnoreCase("delete")) {
            connection.setRequestMethod("DELETE");
        }

        return connection;
    }

    /**
     * Create query string for geoserver rest endpoint
     *
     * @param params
     * @return
     */
    public String getQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(entry.getKey())
                .append("=")
                .append(entry.getValue())
                .append("&");
        }
        queryString.deleteCharAt(queryString.length() - 1); // Remove the trailing "&"
        return queryString.toString();
    }

    /** delete store from geoserver
     *
     * @param store
     * @return
     */
    public boolean deleteStoreFromGeoserver(String store) {
        // to delete the layer, simply delete the store with recursive is true.
        // by doing this, all the layers in the stores will also be removed.
        // since there is no way to find out if the given id is for datastore or coveragestore
        // it will try to delete datastore first, then if it was not successful
        // it will try to delete coveragestore

        boolean isDeleted = false;

        // remove datastore after with recursive option
        try {
            String apiUrl = this.geoserverUrl + "/rest/workspaces/" + GEOSERVER_WORKSPACE + "/datastores/" + store + "?recurse=true";
            HttpURLConnection connection = createHttpConnection(apiUrl, "delete");
            int responseCode = connection.getResponseCode();

            // if datastores deletion failed, do coveragestores deletion
            if (responseCode != 200) {
                apiUrl = this.geoserverUrl + "/rest/workspaces/" + GEOSERVER_WORKSPACE + "/coveragestores/" + store + "?recurse=true";
                connection = createHttpConnection(apiUrl, "delete");
                responseCode = connection.getResponseCode();
            }

            if (responseCode == 200) {
                isDeleted = true;
            }
        } catch (Exception e) {
            logger.error("Failed to Delete to geoserver rest api", e);
            System.out.println(e);
            return false;
        }
        return isDeleted;
    }

}
