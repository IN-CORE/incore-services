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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

/**
 * Created by ywkim on 11/9/2017.
 */


public class GeoserverUtils {

    public static final String GEOSERVER_REST_URL = System.getenv("GEOSERVER_URL");
    public static final String GEOSERVER_USER = System.getenv("GEOSERVER_USER");
    public static final String GEOSERVER_PW = System.getenv("GEOSERVER_PW");
    public static final String GEOSERVER_WORKSPACE = System.getenv("GEOSERVER_WORKSPACE");

    private static final Logger logger = Logger.getLogger(GeoserverUtils.class);


    /**
     * upload file to geoserver
     *
     * @param store
     * @param inFile
     * @param inExt
     * @return
     * @throws MalformedURLException
     * @throws FileNotFoundException
     */
    public static boolean uploadToGeoserver(String store, File inFile, String inExt) throws MalformedURLException, FileNotFoundException {
        GeoServerRESTPublisher publisher = createPublisher();
        String fileName = FilenameUtils.getBaseName(inFile.getName());
        boolean created = publisher.createWorkspace(GEOSERVER_WORKSPACE);
        boolean published = false;
        if (inExt.equalsIgnoreCase("shp")) {
            published = publisher.publishShp(GEOSERVER_WORKSPACE, store, fileName, inFile);
        } else if (inExt.equalsIgnoreCase("asc")) {
            published = publisher.publishArcGrid(GEOSERVER_WORKSPACE, store, inFile);
        } else if (inExt.equalsIgnoreCase("tif")) {
            published = publisher.publishGeoTIFF(GEOSERVER_WORKSPACE, store, inFile);
        }
        return published;
    }

    public static boolean uploadShpZipToGeoserver(String store, File zipFile) throws FileNotFoundException {
        GeoServerRESTPublisher publisher = createPublisher();
        String fileName = FilenameUtils.getBaseName(zipFile.getName());
        boolean created = publisher.createWorkspace(GEOSERVER_WORKSPACE);
        boolean published = publisher.publishShp(GEOSERVER_WORKSPACE, store, fileName, zipFile);
        FileUtils.deleteTmpDir(zipFile);

        return published;
    }

    /**
     * upload dataset to geoserver. This is a preparation process before actual uploading process
     *
     * @param dataset
     * @param repository
     * @param isShp
     * @param isTif
     * @param isAsc
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static boolean datasetUploadToGeoserver(Dataset dataset, IRepository repository,
                                                   boolean isShp, boolean isTif, boolean isAsc) throws IOException, URISyntaxException {
        String datasetId = dataset.getId();
        boolean published = false;
        File outFile = null;
        String inExt = "";

        if (datasetId != null && datasetId.length() > 0) {
            if (isShp) {
                // get zip file
                inExt = "shp";
                outFile = FileUtils.loadFileFromService(dataset, repository, true, inExt);
                // replace extension from zip to shp
                String tmpName = FilenameUtils.removeExtension(outFile.getAbsolutePath());
                String fileName = tmpName + "." + inExt;
                double[] bbox = GeotoolsUtils.getBboxFromShp(new File(fileName));
                dataset.setBoundingBox(bbox);
                repository.addDataset(dataset);
                published = uploadToGeoserver(datasetId, outFile, inExt);
            } else if (isTif == true || isAsc == true) {
                if (isTif) {
                    inExt = "tif";
                } else {
                    inExt = "asc";
                }
                outFile = FileUtils.loadFileFromService(dataset, repository, true, inExt);
                double[] bbox = GeotoolsUtils.getBboxFromGrid(outFile);
                dataset.setBoundingBox(bbox);
                repository.addDataset(dataset);
                published = uploadToGeoserver(datasetId, outFile, inExt);
            }
        }

        FileUtils.deleteTmpDir(outFile);

        return published;
    }

    public static boolean networkDatasetUploadToGeoserver(Dataset dataset, IRepository repository) throws IOException, URISyntaxException {
        String datasetId = dataset.getId();
        boolean link_published = false;
        boolean node_published = false;
        File[] outFiles = null;
        String inExt = "";

        if (datasetId != null && datasetId.length() > 0) {
            // get file name for node and link
            String linkName = dataset.getNetworkDataset().getLink().getFileName();
            String nodeName = dataset.getNetworkDataset().getNode().getFileName();

            // get zip file
            inExt = "shp";
            outFiles = FileUtils.loadNetworkFileFromService(dataset, repository, true, inExt);
            // replace extension from zip to shp
            String linkFileName = FilenameUtils.removeExtension(outFiles[0].getAbsolutePath()) + "." + inExt;
            double[] bbox = GeotoolsUtils.getBboxFromShp(new File(linkFileName));
            dataset.setBoundingBox(bbox);
            repository.addDataset(dataset);
            link_published = uploadToGeoserver(datasetId, outFiles[0], inExt);
            node_published = uploadToGeoserver(datasetId, outFiles[1], inExt);

        }

        FileUtils.deleteTmpDir(outFiles[0]);

        if (link_published == true && node_published == true) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean removeLayerFromGeoserver(String id) {
        GeoServerRESTPublisher publisher = createPublisher();
        return publisher.removeLayer(GEOSERVER_WORKSPACE, id);
    }

    public static boolean removeLayerFromGeoserver(String id, String surfix) {
        GeoServerRESTPublisher publisher = createPublisher();
        return publisher.removeLayer(GEOSERVER_WORKSPACE, id + surfix);
    }

    public static boolean removeStoreFromGeoserver(String id) {
        GeoServerRESTPublisher publisher = createPublisher();
        boolean isRemoved = publisher.removeCoverageStore(GEOSERVER_WORKSPACE, id, false, GeoServerRESTPublisher.Purge.ALL);
        return isRemoved;
    }

    public static GeoServerRESTPublisher createPublisher() {
        return new GeoServerRESTPublisher(GEOSERVER_REST_URL, GEOSERVER_USER, GEOSERVER_PW);
    }

    /**
     * Upload geopackage file to geoserver
     * 
     * @param store
     * @param gpkgFile
     * @return
     * @throws Exception
     */
    public static boolean uploadGpkgToGeoserver(String store, File gpkgFile) {
        String url = GEOSERVER_REST_URL+"/rest/workspaces/" + GEOSERVER_WORKSPACE + "/datastores/"
                + store + "/file.gpkg";
        URI uri = URI.create(url);
        Authentication.Result auth = new BasicAuthentication.BasicResult(uri, GEOSERVER_USER, GEOSERVER_PW);


        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            Request request = httpClient.newRequest(uri);
            request.method(HttpMethod.PUT);
            request.file(gpkgFile.toPath(), "application/x-sqlite3");
    
            auth.apply(request);
            ContentResponse response = request.send();
            int responseStatus = response.getStatus();
            httpClient.stop();
    
            if ( (responseStatus == HttpStatus.CREATED_201) || (responseStatus == HttpStatus.ACCEPTED_202) || (responseStatus == HttpStatus.OK_200)) {
                return true;
            }            
        } catch (Exception e) {
            logger.error("HttpClient error", e);
            return false;
        }

        return false;
    }

}
