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

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geoserver.GeoserverUtils;
import edu.illinois.ncsa.incore.service.data.geotools.GeotoolsUtils;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.impl.FileStorageDisk;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("datasets")
public class DatasetController {
    private static final String DATA_REPO_FOLDER = Config.getConfigProperties().getProperty("data.repo.data.dir");
    private static final String POST_PARAMENTER_NAME = "name";
    private static final String POST_PARAMENTER_FILE = "file";
    private static final String POST_PARAMENTER_META = "parentdataset";
    private static final String POST_PARAMETER_DATASET_ID = "datasetId";
    private static final String UPDATE_OBJECT_NAME = "property name";
    private static final String UPDATE_OBJECT_VALUE = "property value";
    private static final String WEBDAV_SPACE_NAME = "ergo";
    private static final Logger logger = Logger.getLogger(DatasetController.class);

    @Inject
    private IRepository repository;


    @Inject
    private IAuthorizer authorizer;


    /**
     * Returns a list of datasets in the Dataset collection
     *
     * @param datasetId dataset id for querying the dataset content from datase
     * @return dataset object
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset getDatasetFromRepo(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())) {
            throw new ForbiddenException("You are not allowed to access that dataset");
        }

        return dataset;
    }

    /**
     * query dataset by using either title or type or both
     *
     * @param typeStr
     * @param titleStr
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dataset> getDatasets(@HeaderParam("X-Credential-Username") String username,
                                     @QueryParam("type") String typeStr,
                                     @QueryParam("title") String titleStr,
                                     @QueryParam("creator") String creator,
                                     @QueryParam("space") String space
                                     ) {
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

        if (datasets == null) {
            logger.error("Error finding dataset");
            throw new NotFoundException("Error finding dataset");
        }

        return datasets.stream()
            .filter(d -> (creator == null || "".equals(creator.trim()) || creator.trim().equals(d.getCreator())))
            .filter(d -> (space == null || "".equals(space.trim()) || d.getSpaces().contains(space)))
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }

    /**
     * Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset
     *
     * @param datasetId id of the Dataset in mongodb
     * @return
     */
    @GET
    @Path("{id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByDataset(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId) {
        File outFile = null;
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())) {
            throw new ForbiddenException();
        }

        try {
            outFile = FileUtils.loadFileFromService(dataset, repository, false, "");
        } catch (IOException e) {
            logger.error("Error creating temp directory for " + datasetId, e);
            throw new InternalServerErrorException("Error creating temp directory for " + datasetId, e);
        } catch (URISyntaxException e) {
            logger.error("Error creating file with given url for " + datasetId, e);
            throw new InternalServerErrorException("Error creating file with given url for " + datasetId, e);
        }

        if (outFile != null) {
            String fileName = outFile.getName();
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            logger.error("Error finding output zip file for " + datasetId);
            throw new NotFoundException("Error finding output zip file for " + datasetId);
        }
    }

    /**
     * provide list of FileDescriptor by dataset id
     *
     * @param datasetId
     * @return
     */
    @GET
    @Path("{id}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileDescriptor> getDatasets(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())){
            throw new ForbiddenException();
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        if (fds == null) {
            logger.error("Error finding FileDescriptor from the dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding FileDescriptor from the dataset with the id of " + datasetId);
        }
        return fds;
    }

    /**
     * Returns a file that is attached to a FileDescriptor specified by dataset and fileDescriptor id
     *
     * @param id
     * @param fileId
     * @return
     */
    @GET
    @Path("{id}/files/{file_id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFileByFileDescriptor(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String id, @PathParam("file_id") String fileId) {
        File outFile = null;
        Dataset dataset = repository.getDatasetById(id);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + id);
            throw new NotFoundException("Error finding dataset with the id of " + id);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())) {
            throw new ForbiddenException();
        }


        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = "";
        String fdId = "";
        String fileName = "";

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(fileId)) {
                dataUrl = fd.getDataURL();
                fileName = fd.getFilename();
            }
        }

        try {
            if (!dataUrl.equals("")) {
                outFile = new File(new URI(dataUrl));
                outFile.renameTo(new File(outFile.getParentFile(), fileName));
            }
        } catch (URISyntaxException e) {
            logger.error("Error creating file with dataset's location url ", e);
            throw new InternalServerErrorException("Error creating file with dataset's location url ", e);
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            logger.error("Error finding output file.");
            throw new NotFoundException("Error finding output file.");
        }
    }

    /**
     * get file descriptor by datasetid and file descriptor id
     *
     * @param id
     * @param fileId
     * @return
     */
    @GET
    @Path("{id}/files/{file_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FileDescriptor getFileByDatasetIdFileDescriptor(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String id, @PathParam("file_id") String fileId) {
        Dataset dataset = repository.getDatasetById(id);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + id);
            throw new NotFoundException("Error finding dataset with the id of " + id);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())){
            throw new ForbiddenException();
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String fdId = "";
        FileDescriptor fileDescriptor = null;


        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(fileId)) {
                fileDescriptor = fd;
                break;
            }
        }

        if (fileDescriptor == null) {
            logger.error("Error finding FileDescriptor with the id of " + fileId);
            throw new NotFoundException("Error finding FileDescriptor with the id of " + fileId);
        }
        return fileDescriptor;
    }

    /**
     * ingest dataset object using json
     *
     * @param username
     * @param inDatasetJson
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Dataset ingestDataset(@HeaderParam("X-Credential-Username") String username, @FormDataParam("dataset") String inDatasetJson) {
        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }


        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        if (isJsonValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Posted json is not a valid json.");
        }

        String title = "";
        String dataType = "";
        String sourceDataset = "";
        String format = "";
        String fileName = "";
        String description = "";
        List<String> spaces = null;

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            title = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TITLE, inDatasetJson);
            dataType = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TYPE, inDatasetJson);
            sourceDataset = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            format = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FORMAT, inDatasetJson);
            fileName = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FILE_NAME, inDatasetJson);
            spaces = JsonUtils.extractValueListFromJsonString(FileUtils.DATASET_SPACES, inDatasetJson);
            if(!spaces.contains(username)){
                spaces.add(username);
            }
            description = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_DESCRIPTION, inDatasetJson);
            dataset.setTitle(title);
            dataset.setCreator(username);
            dataset.setDataType(dataType);
            dataset.setDescription(description);
            dataset.setSourceDataset(sourceDataset);
            dataset.setFormat(format);
            dataset.setSpaces(spaces);
            dataset.setPrivileges(Privileges.newWithSingleOwner(username));


            dataset = repository.addDataset(dataset);
            if (dataset == null) {
                logger.error("Error finding dataset with the id of " + dataset.getId());
                throw new NotFoundException("Error finding dataset with the id of " + dataset.getId());
            }

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

                    if(!datasetIds.contains(id)){
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
     *
     * @param username
     * @param datasetId
     * @return
     */
    @DELETE
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Dataset deleteDataset(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId) {
        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }


        Dataset dataset = null;
        dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canWrite(username, dataset.getPrivileges())){
            throw new ForbiddenException();
        }

        String creator = dataset.getCreator();
        List<String> spaces = dataset.getSpaces();

        if (creator != null) {
            if (creator.equals(username)) {
                // remove dataset
                dataset = repository.deleteDataset(datasetId);
                if (dataset != null) {
                    // remove files
                    List<FileDescriptor> fds = dataset.getFileDescriptors();
                    if (fds.size() > 0) {
                        for (FileDescriptor fd : fds) {
                            try {
                                File file = new File((new URL(fd.getDataURL())).toURI());
                                FileUtils.deleteTmpDir(file);
                            } catch (MalformedURLException e) {
                                logger.error("Error creating URL using dataset location ", e);
                                throw new InternalServerErrorException("Error creating URL using dataset location ", e);
                            } catch (URISyntaxException e) {
                                logger.error("Error converting data url to uri ", e);
                                throw new InternalServerErrorException("Error converting data url to uri ", e);
                            }
                        }
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
     *
     * @param datasetId
     * @param inputs
     * @return
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/files")
    public Dataset uploadFiles(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId, FormDataMultiPart inputs) {

        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }

        int bodyPartSize = inputs.getBodyParts().size();
        String objIdStr = datasetId;
        String inJson = "";
        String paramName = "";
        Dataset dataset = repository.getDatasetById(objIdStr);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canWrite(username, dataset.getPrivileges())){
            throw new ForbiddenException();
        }

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
                if (fileExt.equalsIgnoreCase("shp") || fileExt.equalsIgnoreCase("asc") ||
                    fileExt.equalsIgnoreCase("tif")) {
                    isGeoserver = true;
                    if (fileExt.equalsIgnoreCase("asc")) {
                        isAsc = true;
                    } else if (fileExt.equalsIgnoreCase("tif")) {
                        isTif = true;
                    } else if (fileExt.equalsIgnoreCase("shp")) {
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
                    logger.error("Error storing files of the dataset with the id of " + datasetId);
                    throw new NotFoundException("Error string files of the dataset with the id of " + datasetId);
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
        if (sourceDataset.length() > 0 && format.equalsIgnoreCase("table")) {
            isJoin = true;
            isGeoserver = true;
        }

        // create GUID if there is no GUID in the table
        List<FileDescriptor> shpFDs = dataset.getFileDescriptors();
        List<File> files = new ArrayList<File>();
        File zipFile = null;
        boolean isShpfile = false;

        if (format.equalsIgnoreCase(FileUtils.FORMAT_SHAPEFILE)) {
            try {
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
            } catch (URISyntaxException e) {
                logger.error("Error creating file from dataset locatoin ", e);
                throw new InternalServerErrorException("Error creating file from dataset location ", e);
            }
            try {
                boolean isGuid = GeotoolsUtils.createGUIDinShpfile(dataset, files);
                if (isGuid) {
                    logger.debug("The shapefile already has guid field");
                }
            } catch (IOException e) {
                logger.error("Error creating temp directory in guid creation process ", e);
                throw new InternalServerErrorException("Error creating temp directory in guid creation process ", e);
            }
        }

        if (isGeoserver) {
            if (isJoin) {
                try {
                    zipFile = FileUtils.joinShpTable(dataset, repository, true);
                    GeoserverUtils.uploadShpZipToGeoserver(dataset.getId(), zipFile);
                } catch (IOException e) {
                    logger.error("Error making temp directory in joining process ", e);
                    throw new InternalServerErrorException("Error making temp directory in joining process ", e);
                } catch (URISyntaxException e) {
                    logger.error("Error making file using dataset's location url in table join process ", e);
                    throw new InternalServerErrorException("Error making file using dataset's location uri in table join process ", e);
                }
            } else {
                try {
                    GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
                } catch (IOException e) {
                    logger.error("Error uploading dataset to geoserver ", e);
                    throw new InternalServerErrorException("Error uploading dataset to geoserver ", e);
                } catch (URISyntaxException e) {
                    logger.error("Error making file using dataset's location url ", e);
                    throw new InternalServerErrorException("Error making file using dataset's location uri ", e);
                }
            }
        }

        return dataset;
    }

    /**
     * file(s) to upload to attach to a dataset by FileDescriptor
     *
     * @param datasetId
     * @param inDatasetJson
     * @return
     */
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Object updateObject(@HeaderParam("X-Credential-Username") String username, @PathParam("id") String datasetId, @FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        if (isJsonValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Posted json is not a valid json.");
        }

        Dataset dataset = null;
        dataset = repository.getDatasetById(datasetId);
        if (!(authorizer.canWrite(username, dataset.getPrivileges()))) {
            throw new ForbiddenException();
        }


        if (isJsonValid) {
            String propName = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_NAME, inDatasetJson);
            String propVal = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_VALUE, inDatasetJson);
            dataset = repository.updateDataset(datasetId, propName, propVal);
        }

        return dataset;
    }
}
