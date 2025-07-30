/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.utils;

import com.opencsv.exceptions.CsvValidationException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import jakarta.ws.rs.core.Response;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.opencsv.CSVReader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;


public class ServiceUtils {
    public static final String HAZARD_STATUS_ENDPOINT = "hazard/api/status/usage";
    public static final String X_AUTH_USERINFO = "x-auth-userinfo";

    /**
     * utility for creating data endpoint string
     *
     * @return
     */
    public static String createHazardEndpoint() {
        String dataEndpoint = "http://localhost:8080/";
        String dataEndpointProp = System.getenv("HAZARD_SERVICE_URL");
        if (dataEndpointProp != null && !dataEndpointProp.isEmpty()) {
            dataEndpoint = dataEndpointProp;
            if (!dataEndpoint.endsWith("/")) {
                dataEndpoint += "/";
            }
        }

        return dataEndpoint;
    }

    /**
     * utility for calling hazard usage end point
     *
     * @param userInfo
     * @return
     * @throws IOException
     */
    public static String getHazardUsage(String userInfo) throws IOException {
        // parse username
        String userName = DataJsonUtils.parseUserName(userInfo);

        if (userName == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the username");
        }

        // access earthquake usage enpoint
        String hazardEndpoint = ServiceUtils.createHazardEndpoint();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String requestUrl = "";
        requestUrl = hazardEndpoint + HAZARD_STATUS_ENDPOINT;

        HttpGet httpGet = new HttpGet(requestUrl);
        httpGet.setHeader(X_AUTH_USERINFO, "{\"preferred_username\": \"" + userName + "\"}");

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;

        response = httpclient.execute(httpGet);
        responseStr = responseHandler.handleResponse(response);

        return responseStr;
    }

    /**
     * Validates whether the child and parent datasets are join-compatible based on their GUID values.
     *
     * The child dataset must be in CSV format, and the parent dataset must be a shapefile (DBF inside ZIP).
     * This method reads the GUIDs from both datasets and verifies that at least one GUID is shared between them.
     * If either file cannot be loaded, the method returns false without throwing.
     *
     * @param child      the child dataset (expected to be CSV)
     * @param parent     the parent dataset (expected to be a zipped shapefile)
     * @param repository dataset repository used to retrieve file descriptors and data
     * @return true if the datasets share at least one GUID; false otherwise
     */
    public static boolean validateJoinCompatibilityByGuid(Dataset child, Dataset parent, IRepository repository) {
        File childCsvZip = null;
        File parentShapefileZip = null;

        try {
            childCsvZip = FileUtils.loadFileFromService(child, repository, false, "");
            parentShapefileZip = FileUtils.loadFileFromService(parent, repository, false, "");

            if (childCsvZip == null || parentShapefileZip == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "One or both datasets are not available or invalid format for join with source dataset.");
            }

            Set<String> childGuids = extractGuidFromCsv(childCsvZip);
            Set<String> parentGuids = extractGuidFromDbfInZip(parentShapefileZip);

            if (childGuids == null || parentGuids == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "One or both datasets are not available or invalid format for join with source dataset.");
            }

            if (childGuids.isEmpty() || parentGuids.isEmpty()) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "One or both datasets are empty or invalid format for join with source dataset.");
            }

            Set<String> intersection = new HashSet<>(childGuids);
            intersection.retainAll(parentGuids);

            if (intersection.isEmpty()) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST,
                    "No matching GUIDs found between source and parent datasets.");
            }

            return true;

        } catch (IOException | URISyntaxException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to load dataset files during join.");
        } finally {
            if (childCsvZip != null && childCsvZip.exists()) {
                childCsvZip.delete();
            }
            if (parentShapefileZip != null && parentShapefileZip.exists()) {
                parentShapefileZip.delete();
            }
        }
    }

    /**
     * Extracts GUID values from a CSV file.
     *
     * @param csvFile the input CSV file
     * @return a set of GUID strings from the CSV
     * @throws IncoreHTTPException if the file cannot be read or the GUID column is missing
     */
    private static Set<String> extractGuidFromCsv(File zipFile) {
        Set<String> guids = new HashSet<>();
        File tempCsv = null;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".csv")) {
                    // Copy the CSV entry to a temporary file
                    tempCsv = File.createTempFile("child", ".csv");
                    try (FileOutputStream fos = new FileOutputStream(tempCsv)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    break; // Exit loop after finding first CSV
                }
            }

            // If CSV wasn't found
            if (tempCsv == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No CSV file found in the ZIP.");
            }

            // Now read the temporary CSV file
            try (CSVReader reader = new CSVReader(new FileReader(tempCsv))) {
                String[] headers = reader.readNext();
                if (headers == null) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "CSV file missing header.");
                }

                int guidIndex = -1;
                for (int i = 0; i < headers.length; i++) {
                    if ("guid".equalsIgnoreCase(headers[i])) {
                        guidIndex = i;
                        break;
                    }
                }

                if (guidIndex == -1) {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "CSV file missing GUID column.");
                }

                String[] row;
                while ((row = reader.readNext()) != null) {
                    if (guidIndex < row.length && row[guidIndex] != null && !row[guidIndex].isEmpty()) {
                        guids.add(row[guidIndex].trim());
                    }
                }

            } catch (IOException | CsvValidationException e) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error reading CSV file.");
            }

        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error reading ZIP file.");
        } finally {
            if (tempCsv != null && tempCsv.exists()) {
                tempCsv.delete(); // clean up
            }
        }

        return guids;
    }


    /**
     * Extracts GUID values from a DBF file contained within a zipped shapefile.
     *
     * The method looks for the .dbf entry in the ZIP archive, reads it using a DBF reader,
     * and extracts all non-null GUID values.
     *
     * @param zipFile a ZIP file containing a shapefile (including a .dbf)
     * @return a set of GUID strings extracted from the DBF file
     * @throws IncoreHTTPException if reading fails or GUID column is not found
     */
    public static Set<String> extractGuidFromDbfInZip(File zipFile) {
        Set<String> guids = new HashSet<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".dbf")) {
                    // Copy .dbf from ZIP to temp file
                    File tempDbf = File.createTempFile("extracted", ".dbf");
                    try (FileOutputStream fos = new FileOutputStream(tempDbf)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    // Read DBF file using GeoTools
                    try (SeekableByteChannel channel = Files.newByteChannel(tempDbf.toPath(), StandardOpenOption.READ);
                         DbaseFileReader dbfReader = new DbaseFileReader(channel, false, Charset.defaultCharset())) {

                        DbaseFileHeader header = dbfReader.getHeader();
                        int guidIndex = -1;
                        for (int i = 0; i < header.getNumFields(); i++) {
                            if ("guid".equalsIgnoreCase(header.getFieldName(i))) {
                                guidIndex = i;
                                break;
                            }
                        }

                        if (guidIndex == -1) {
                            return Collections.emptySet(); // No GUID column
                        }

                        while (dbfReader.hasNext()) {
                            Object[] row = dbfReader.readEntry();
                            if (row[guidIndex] != null) {
                                guids.add(row[guidIndex].toString().trim());
                            }
                        }

                    } finally {
                        tempDbf.delete(); // Clean up
                    }

                    break; // Stop after the first .dbf found
                }
            }
        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to read DBF from shapefile.");
        }

        return guids;
    }


}
