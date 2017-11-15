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
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

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
     * @param store
     * @param inFile
     * @param inExt
     * @return
     * @throws MalformedURLException
     * @throws FileNotFoundException
     */
    public static boolean uploadToGeoserver(String store, File inFile, String inExt) throws MalformedURLException, FileNotFoundException {
        GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(GEOSERVER_REST_URL, GEOSERVER_USER, GEOSERVER_PW);
        String fileName = FilenameUtils.getBaseName(inFile.getName());
        boolean created = publisher.createWorkspace(GEOSERVER_WORKSPACE);
        boolean published = false;
        if (inExt.equalsIgnoreCase("shp")) {    //$NON-NLS-1$
            published = publisher.publishShp(GEOSERVER_WORKSPACE, store, fileName, inFile);    //$NON-NLS-1$
        } else if (inExt.equalsIgnoreCase("asc")) { //$NON-NLS-1$
            published = publisher.publishArcGrid(GEOSERVER_WORKSPACE, store, inFile);
        } else if (inExt.equalsIgnoreCase("tif")) { //$NON-NLS-1$
            published = publisher.publishGeoTIFF(GEOSERVER_WORKSPACE, store, inFile);
        }
        return published;
    }


    public GeoserverUtils() throws MalformedURLException {
    }
}
