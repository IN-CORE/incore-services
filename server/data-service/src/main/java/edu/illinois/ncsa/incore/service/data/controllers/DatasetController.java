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

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geoserver.GeoserverUtils;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.MvzLoader;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("datasets")
public class DatasetController {
    private static final String DATA_REPO_FOLDER = Config.getConfigProperties().getProperty("data.repo.data.dir");  //$NON-NLS-1$
    private static final String POST_PARAMENTER_NAME = "name";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_FILE = "file";  //$NON-NLS-1$
    private static final String POST_PARAMENTER_META = "parentdataset";  //$NON-NLS-1$
    private static final String POST_PARAMETER_DATASET_ID = "datasetId";    //$NON-NLS-1$
    private static final String UPDATE_OBJECT_NAME = "property name";  //$NON-NLS-1$
    private static final String UPDATE_OBJECT_VALUE = "property value";  //$NON-NLS-1$
    private static final String WEBDAV_SPACE_NAME = "ergo";   //$NON-NLS-1$
    private Logger logger = Logger.getLogger(DatasetController.class);

    @Inject
    private IRepository repository;

    /**
     * Returns a list of datasets in the Dataset collection
     *
     * @param datasetId dataset id for querying the dataset content from datase
     * @return dataset object
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getDatasetFromRepo(@PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            throw new NotFoundException("There is no Dataset with given id in the repository.");
        }

        return dataset;
    }

    /**
     * query dataset by using either title or type or both
     * @param typeStr
     * @param titleStr
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasets(@QueryParam("type") String typeStr, @QueryParam("title") String titleStr) {
        List<Dataset> datasets = null;
        if (typeStr != null && titleStr == null) {  // query only for the type
            datasets = repository.getDatasetByType(typeStr);
        } else if (typeStr == null && titleStr != null) {   // query only for the title
            datasets = repository.getDatasetByTitle(titleStr);
        } else if (typeStr != null && titleStr != null) {   // query for both type and title
            datasets = repository.getDatasetByTypeAndTitle(typeStr, titleStr);
        } else {
            datasets = repository.getAllDatasets();
        }

        return datasets;
    }

    /**
     * Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset
     * @param datasetId id of the Dataset in mongodb
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByDataset(@PathParam("id") String datasetId) throws IOException, URISyntaxException {
        File outFile = FileUtils.loadFileFromService(datasetId, repository, false, "");
        String fileName = outFile.getName();

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return Response.status(404).build();
        }
    }

    /**
     * provide list of FileDescriptor by dataset id
     * @param datasetId
     * @return
     */
    @GET
    @Path("/{id}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileDescriptor> getDatasets(@PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        return fds;
    }

    /**
     * Returns a file that is attached to a FileDescriptor specified by dataset and fileDescriptor id
     * @param id
     * @param fileId
     * @return
     * @throws URISyntaxException
     */
    @GET
    @Path("/{id}/files/{file_id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByFileDescriptor(@PathParam("id") String id, @PathParam("file_id") String fileId) throws URISyntaxException {
        File outFile = null;
        Dataset dataset = repository.getDatasetById(id);

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = ""; //$NON-NLS-1$
        String fdId = "";   //$NON-NLS-1$
        String fileName = "";   //$NON-NLS-1$

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(fileId)) {
                dataUrl = fd.getDataURL();
                fileName = fd.getFilename();
            }
        }

        if (!dataUrl.equals("")) {  //$NON-NLS-1$
            outFile = new File(new URI(dataUrl));
            outFile.renameTo(new File(outFile.getParentFile(), fileName));
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Path("/{id}/files/{file_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FileDescriptor getFileByDatasetIdFileDescriptor(@PathParam("id") String id, @PathParam("file_id") String fileId) throws URISyntaxException {
        Dataset dataset = repository.getDatasetById(id);
        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String fdId = "";   //$NON-NLS-1$
        FileDescriptor fileDescriptor = null;

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(fileId)) {
                fileDescriptor = fd;
            }
        }

        return fileDescriptor;
    }

    /**
     * ingest dataset object using json
     * @param username
     * @param inDatasetJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset ingestDataset(@HeaderParam("X-Credential-Username") String username, @FormDataParam("dataset") String inDatasetJson) {
        // example input json
        //
        //{ schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0", title: "shelby building damage", sourceDataset: "59e5098168f47426547409f3", format: "csv", spaces: ["ywkim", "ergo"] }
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        String title = "";
        String type = "";
        String sourceDataset = "";
        String format = "";
        String fileName = "";
        List<String> spaces = null;

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            title = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TITLE, inDatasetJson);
            type = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TYPE, inDatasetJson);
            sourceDataset = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            format = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FORMAT, inDatasetJson);
            fileName = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FILE_NAME, inDatasetJson);
            spaces = JsonUtils.extractValueListFromJsonString(FileUtils.DATASET_SPACES, inDatasetJson);
            dataset.setTitle(title);
            dataset.setCreator(username);
            dataset.setDataType(type);
            dataset.setSourceDataset(sourceDataset);
            dataset.setFormat(format);
            dataset.setSpaces(spaces);

            dataset = repository.addDataset(dataset);

            String id = dataset.getId();

            // insert/update space
            for (String spaceName : spaces) {
                Space foundSpace = repository.getSpaceByName(spaceName);
                if (foundSpace == null) {   // new space: insert the data
                    Space space = new Space();
                    space.setName(spaceName);
                    List<String> datasetIds = new ArrayList<String>();
                    datasetIds.add(id);
                    space.setDatasetIds(datasetIds);
                    repository.addSpace(space);
                } else {    // the space with space name exists
                    // get dataset ids
                    List<String> datasetIds = foundSpace.getDatasetIds();
                    boolean isIdExists = false;
                    for (String datasetId : datasetIds) {
                        if (datasetId.equals(id)) {
                            isIdExists = true;
                        }
                    }
                    if (!isIdExists) {
                        foundSpace.addDatasetId(id);
                        // this will update it since the objectId is identical
                        repository.addSpace(foundSpace);
                    }
                }
            }
        }
        return dataset;
    }

    /**
     * delete dataset from database and attached information like files and geoserver layer
     * @param username
     * @param datasetId
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @DELETE
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Dataset deleteDataset(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId) throws MalformedURLException, URISyntaxException {
        Dataset dataset = null;
        dataset = repository.getDatasetById(datasetId);
        String creator = dataset.getCreator();
        List<String> spaces = dataset.getSpaces();

        if (creator != null) {
            if (creator.equals(username)) {
                // remove dataset
                dataset = repository.deleteDataset(datasetId);
                if (dataset != null) {

                    // remove files
                    List<FileDescriptor> fds = dataset.getFileDescriptors();
                    for (FileDescriptor fd : fds) {
                        File file = new File((new URL(fd.getDataURL())).toURI());
                        FileUtils.deleteTmpDir(file);
                    }

                    // remove geoserver layer
                    boolean layerRemoved = GeoserverUtils.removeLayerFromGeoserver(datasetId);

                    // remove id from space
                    for (String spaceStr : spaces) {
                        Space space = repository.getSpaceByName(spaceStr);
                        repository.removeIdFromSpace(space, datasetId);
                        repository.addSpace(space);
                    }
                }
            } else {
                dataset = null;
            }
        }

        return dataset;
    }

    /**
     * upload file(s) to attach to a dataset by FileDescriptor
     * @param datasetId
     * @param inputs
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/files")
    public Dataset uplaodFiles(@PathParam("id") String datasetId, FormDataMultiPart inputs) throws IOException, URISyntaxException {
        int bodyPartSize = inputs.getBodyParts().size();
        String objIdStr = datasetId;
        String inJson = ""; //$NON-NLS-1$
        String paramName = "";  //$NON-NLS-1$
        Dataset dataset = repository.getDatasetById(objIdStr);
        boolean isJsonValid = false;
        boolean isGeoserver = false;
        boolean isAsc = false;
        boolean isShp = false;
        boolean isTif = false;
        boolean isJoin = false;

        int j = 0;
        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_FILE)) {
                String fileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                String fileExt = FilenameUtils.getExtension(fileName);
                if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("asc") ||   //$NON-NLS-1$ //$NON-NLS-2$
                        fileExt.equalsIgnoreCase("tif")) {  //$NON-NLS-1
                    isGeoserver = true;
                    if (fileExt.equalsIgnoreCase("asc")) {  //$NON-NLS-1$
                        isAsc = true;
                    } else if (fileExt.equalsIgnoreCase("tif")) {   //$NON-NLS-1$
                        isTif = true;
                    } else if (fileExt.equalsIgnoreCase("shp")) {   //$NON-NLS-1$
                        isShp = true;
                    }
                }
                InputStream is = (InputStream) inputs.getFields(POST_PARAMENTER_FILE).get(j).getValueAs(InputStream.class);
                FileDescriptor fd = new FileDescriptor();
                FileStorageDisk fsDisk = new FileStorageDisk();

                fsDisk.setFolder(DATA_REPO_FOLDER);
                try {
                    fd = fsDisk.storeFile(fileName, is);
                    fd.setFilename(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataset.addFileDescriptor(fd);
                j++;
            }
        }
        repository.addDataset(dataset);

        // check if there is a source dataset, if so it will be joined to source dataset
        String format = dataset.getFormat();
        String sourceDataset = dataset.getSourceDataset();
        // join it if it is a table dataset with source dataset existed
        if (sourceDataset.length() > 0 && format.equalsIgnoreCase("table")) {   //$NON-NLS-1$
            isJoin = true;
            isGeoserver = true;
        }

        if (isGeoserver) {
            if (isJoin) {
                File zipFile = FileUtils.joinShpTable(dataset, repository, true);
                boolean published = GeoserverUtils.uploadShpZipToGeoserver(dataset.getId(), zipFile);
            } else {
                boolean published = GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
            }
        }

        // create GUID if there is no GUID in the table
        List<FileDescriptor> shpFDs = dataset.getFileDescriptors();
        List<File> files = new ArrayList<File>();
        File zipFile = null;
        boolean isShpfile = false;

        if (format.equalsIgnoreCase(FileUtils.FORMAT_SHAPEFILE)) {
            for (int i = 0; i < shpFDs.size(); i++) {
                FileDescriptor sfd = shpFDs.get(i);
                String shpLoc = sfd.getDataURL();
                File shpFile = new File(new URI(shpLoc));
                files.add(shpFile);
                //get file, if the file is in remote, use http downloader
                String fileExt = FilenameUtils.getExtension(shpLoc);
                if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                    isShpfile = true;
                }
            }
            boolean isGuid = GeotoolsUtils.createGUIDinShpfile(dataset, files);
            if (isGuid) {
                logger.debug("The shapefile already has guid field");   //$NON-NLS-1$
            }
        }

        return dataset;
    }

    /**
     * file(s) to upload to attach to a dataset by FileDescriptor
     * @param datasetId
     * @param inDatasetJson
     * @return
     */
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Object updateObject(@PathParam("id") String datasetId, @FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        Dataset dataset = null;

        if (isJsonValid) {
            String propName = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_NAME, inDatasetJson);
            String propVal = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_VALUE, inDatasetJson);
            dataset = repository.updateDataset(datasetId, propName, propVal);
        }

        return dataset;
    }
}
