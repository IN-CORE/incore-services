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

package edu.illinois.ncsa.incore.service.data.geoserver;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by ywkim on 11/9/2017.
 */


public class GeoserverUtils {

    public static final String GEOSERVER_REST_URL = Config.getConfigProperties().getProperty("geoserver.url");  //$NON-NLS-1$
    public static final String GEOSERVER_USER = Config.getConfigProperties().getProperty("geoserver.user"); //$NON-NLS-1$
    public static final String GEOSERVER_PW = Config.getConfigProperties().getProperty("geoserver.pw"); //$NON-NLS-1$
    public static final String GEOSERVER_WORKSPACE = Config.getConfigProperties().getProperty("geoserver.workspace");   //$NON-NLS-1$

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
        GeoServerRESTPublisher publisher = setPublisher();
        String fileName = FilenameUtils.getBaseName(inFile.getName());
        boolean created = publisher.createWorkspace(GEOSERVER_WORKSPACE);
        boolean published = false;
        if (inExt.equalsIgnoreCase("shp")) {    //$NON-NLS-1$
            published = publisher.publishShp(GEOSERVER_WORKSPACE, store, fileName, inFile);
        } else if (inExt.equalsIgnoreCase("asc")) { //$NON-NLS-1$
            published = publisher.publishArcGrid(GEOSERVER_WORKSPACE, store, inFile);
        } else if (inExt.equalsIgnoreCase("tif")) { //$NON-NLS-1$
            published = publisher.publishGeoTIFF(GEOSERVER_WORKSPACE, store, inFile);
        }
        return published;
    }

    public static boolean uploadShpZipToGeoserver(String store, File zipFile) throws FileNotFoundException {
        GeoServerRESTPublisher publisher = setPublisher();
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
        String inExt = "";  //$NON-NLS-1$

        if (datasetId != null && datasetId.length() > 0) {
            if (isShp) {
                // get zip file
                inExt = "shp";  //$NON-NLS-1$
                outFile = FileUtils.loadFileFromService(datasetId, repository, true, inExt);
                String fileName = outFile.getName();
                published = uploadToGeoserver(datasetId, outFile, inExt);
            } else if (isTif) {
                inExt = "tif";  //$NON-NLS-1$
                outFile = FileUtils.loadFileFromService(datasetId, repository, true, inExt);
                published = uploadToGeoserver(datasetId, outFile, inExt);
            } else if (isAsc) {
                inExt = "asc";  //$NON-NLS-1$
                outFile = FileUtils.loadFileFromService(datasetId, repository, true, inExt);
                published = uploadToGeoserver(datasetId, outFile, inExt);
            }
        }

        FileUtils.deleteTmpDir(outFile);

        return published;
    }

    public static boolean removeLayerFromGeoserver(String id) {
        GeoServerRESTPublisher publisher = setPublisher();
        return publisher.removeLayer(GEOSERVER_WORKSPACE, id);
    }

    public static boolean removeStoreFromGeoserver(String id) {
        GeoServerRESTPublisher publisher = setPublisher();
        return publisher.removeCoverageStore(GEOSERVER_WORKSPACE, id, false);
    }

    public static GeoServerRESTPublisher setPublisher() {
        return new GeoServerRESTPublisher(GEOSERVER_REST_URL, GEOSERVER_USER, GEOSERVER_PW);
    }
}
