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

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by ywkim on 11/9/2017.
 */


public class GeoserverUtils {
    private static final Logger logger = Logger.getLogger(GeoserverUtils.class);


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
            GeoserverRestApi gsApi = GeoserverRestApi.createGeoserverApi();
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
                // layer in geoserver doesn't have to be renamed
                published = gsApi.uploadToGeoserver(datasetId, outFile, inExt, false);
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
                // layer in geoserver doesn't have to be renamed
                published = gsApi.uploadToGeoserver(datasetId, outFile, inExt, false);
            }
        }

        FileUtils.deleteTmpDir(outFile);

        return published;
    }

    public static boolean uploadShapefileToGeoserver(String datasetId, File file) {
        GeoserverRestApi gsApi = GeoserverRestApi.createGeoserverApi();
        boolean published = gsApi.uploadToGeoserver(datasetId, file, "shp", false);
        return published;
    }

    public static boolean networkDatasetUploadToGeoserver(Dataset dataset, IRepository repository) throws IOException, URISyntaxException {
        String datasetId = dataset.getId();
        boolean link_published = false;
        boolean node_published = false;
        File[] outFiles = null;
        String inExt = "";

        if (datasetId != null && datasetId.length() > 0) {
            GeoserverRestApi gsApi = GeoserverRestApi.createGeoserverApi();
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
            // layer in geoserver doesn't have to be renamed
            link_published = gsApi.uploadToGeoserver(datasetId, outFiles[0], inExt, false);
            node_published = gsApi.uploadToGeoserver(datasetId, outFiles[1], inExt, false);

        }

        FileUtils.deleteTmpDir(outFiles[0]);

        return link_published == true && node_published == true;
    }

    /**
     * remove store from geoserver with store id
     * @param id
     * @return
     */
    public static boolean removeStoreFromGeoserver(String id) {
        GeoserverRestApi gsApi = GeoserverRestApi.createGeoserverApi();
        boolean isRemoved = gsApi.deleteStoreFromGeoserver(id);

        return isRemoved;
    }

    /**
     * Upload geopackage file to geoserver
     *
     * @param store
     * @param gpkgFile
     * @return
     */
    public static boolean uploadGpkgToGeoserver(String store, File gpkgFile) {
        GeoserverRestApi gsApi = GeoserverRestApi.createGeoserverApi();
        // layer in geoserver doesn't have to be renamed to dataset id
        boolean isPublished = gsApi.uploadToGeoserver(store, gpkgFile, "gpkg", true);

        return isPublished;
    }
}
