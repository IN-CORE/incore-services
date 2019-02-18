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
import edu.illinois.ncsa.incore.service.data.models.Network.Component;
import edu.illinois.ncsa.incore.service.data.models.Network.Graph;
import edu.illinois.ncsa.incore.service.data.models.Network.Link;
import edu.illinois.ncsa.incore.service.data.models.Network.Node;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
import io.swagger.annotations.*;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ywkim on 7/26/2017.
 */

@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Data Service for creating and accessing datasets",
        version = "v0.2.0",
        title = "IN-CORE v2 Data Services API",
        contact = @Contact(
            name = "Jong S. Lee",
            email = "jonglee@illinois.edu",
            url = "http://resilience.colostate.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    ),
    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}

)

@Api(value = "datasets", authorizations = {})

@Path("datasets")
public class DatasetController {
    private static final String DATA_REPO_FOLDER = Config.getConfigProperties().getProperty("data.repo.data.dir");
    private static final String GEOSERVER_ENABLE = Config.getConfigProperties().getProperty("geoserver.enable");
    private static final String POST_PARAMENTER_NAME = "name";
    private static final String POST_PARAMENTER_FILE = "file";
    private static final String POST_PARAMENTER_FILE_LINK = "link-file";
    private static final String POST_PARAMENTER_FILE_NODE = "node-file";
    private static final String POST_PARAMENTER_FILE_GRAPH = "graph-file";
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


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a dataset from the Dataset collection", notes = "")
    public Dataset getDatasetbyId(@HeaderParam("X-Credential-Username") String username,
                                  @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a list of datasets", notes = "Can filter by type, title, creator etc.")
    public List<Dataset> getDatasets(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "DataType of IN-CORE datasets. Can filter by partial datatype strings. ex: ergo:buildingInventoryVer5, ergo:census",
                                         required = false) @QueryParam("type") String typeStr,
                                     @ApiParam(value = "Title of dataset. Can filter by partial title strings", required = false) @QueryParam("title") String titleStr,
                                     @ApiParam(value = "Username of the creator", required = false) @QueryParam("creator") String creator,
                                     @ApiParam(value = "IN-CORE space the datasets belong to. ex: ergo, incore etc.", required = false) @QueryParam("space") String space
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

    @GET
    @Path("{id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Returns a zip file that contains all the files attached to a dataset specified by {id}", notes = "")
    public Response getFileByDataset(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
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


    @GET
    @Path("{id}/files")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of files associated with the dataset and their metadata", notes = "")

    public List<FileDescriptor> getDatasetsFiles(@HeaderParam("X-Credential-Username") String username,
                                                 @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding dataset with the id of " + datasetId);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())) {
            throw new ForbiddenException();
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        if (fds == null) {
            logger.error("Error finding FileDescriptor from the dataset with the id of " + datasetId);
            throw new NotFoundException("Error finding FileDescriptor from the dataset with the id of " + datasetId);
        }
        return fds;
    }


    @GET
    @Path("{id}/files/{file_id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Returns a file that is attached to a FileDescriptor of a dataset", notes = "")
    public Response getFileByFileDescriptor(@HeaderParam("X-Credential-Username") String username,
                                            @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String id,
                                            @ApiParam(value = "FileDescriptor Object Id", required = true) @PathParam("file_id") String fileId) {
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
                dataUrl = FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL());
                fileName = fd.getFilename();
            }
        }

        if (!dataUrl.equals("")) {
            outFile = new File(dataUrl);
            outFile.renameTo(new File(outFile.getParentFile(), fileName));
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            logger.error("Error finding output file.");
            throw new NotFoundException("Error finding output file.");
        }
    }


    @GET
    @Path("{id}/files/{file_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets metadata of a file associated to a dataset", notes = "")
    public FileDescriptor getFileByDatasetIdFileDescriptor(@HeaderParam("X-Credential-Username") String username,
                                                           @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String id,
                                                           @ApiParam(value = "FileDescriptor Object Id", required = true) @PathParam("file_id") String fileId) {
        Dataset dataset = repository.getDatasetById(id);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + id);
            throw new NotFoundException("Error finding dataset with the id of " + id);
        }

        if (!authorizer.canRead(username, dataset.getPrivileges())) {
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

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ingest dataset object as json", notes = "Files have to uploaded to the dataset separately using {id}/files endpoint")
    public Dataset ingestDataset(@HeaderParam("X-Credential-Username") String username,
                                 @ApiParam(value = "JSON representing an input dataset", required = true) @FormDataParam("dataset") String inDatasetJson) {
        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }

        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        if (isJsonValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Posted json is not a valid json.");
        }

        boolean isDatasetParameterValid = JsonUtils.isDatasetParameterValid(inDatasetJson);
        if (isDatasetParameterValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Posted json has wrong parameter");
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
            if (!spaces.contains(username)) {
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

            // add network information in the dataset
            if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                Component component = JsonUtils.createNetworkComponent(inDatasetJson);
                dataset.setComponent(component);
            }

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

                    if (!datasetIds.contains(id)) {
                        foundSpace.addDatasetId(id);
                        // this will update it since the objectId is identical
                        repository.addSpace(foundSpace);
                    }
                }
            }
        }

        return dataset;
    }


    @DELETE
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    @ApiOperation(value = "Deletes a dataset", notes = "Also deletes attached information like files and geoserver layer")
    public Dataset deleteDataset(@HeaderParam("X-Credential-Username") String username,
                                 @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
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

        if (!authorizer.canWrite(username, dataset.getPrivileges())) {
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
                            File file = new File(FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL()));
                            FileUtils.deleteTmpDir(file);

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


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/files")
    @ApiOperation(value = "Upload file(s) to attach to a dataset", notes = "GIS files like shp, tif etc. are also uploaded to IN-CORE geoserver")
    public Dataset uploadFiles(@HeaderParam("X-Credential-Username") String username,
                               @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId,
                               @ApiParam(value = "Form inputs representing the file(s). The id/key of each input file has to be 'file'", required = true)
                                   FormDataMultiPart inputs) {

        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }

        // adding geoserver flag
        // if this flas is false, the data will not be uploaded to geoserver
        boolean enableGeoserver = false;
        if (GEOSERVER_ENABLE.equalsIgnoreCase("true")) {
            enableGeoserver = true;
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

//        if (!authorizer.canWrite(username, dataset.getPrivileges())) {
//            throw new ForbiddenException();
//        }

        // get data format to see if it is a network dataset
        String format = dataset.getFormat();
        String linkFileName = null;
        String nodeFileName = null;
        String graphFileName = null;

        // check if there is link, node, and graph files are presented in the bodypart
        if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
            boolean isLinkPresented = false;
            boolean isNodePresented = false;
            boolean isGraphPresented = false;

            for (int i = 0; i < bodyPartSize; i++) {
                paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
                if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_LINK)) {
                    String tmpName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    String fileExt = FilenameUtils.getExtension(tmpName);
                    if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                        isLinkPresented = true;
                        linkFileName = tmpName;
                    }
                } else if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_NODE)) {
                    String tmpName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    String fileExt = FilenameUtils.getExtension(tmpName);
                    if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                        isNodePresented = true;
                        nodeFileName = tmpName;
                    }
                } else if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_GRAPH)) {
                    graphFileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    isGraphPresented = true;
                }
            }

            if (isLinkPresented == false) {
                logger.error("Error finding link file");
                throw new NotFoundException("Error finding link file with the id of " + datasetId);
            } else if (isNodePresented == false) {
                logger.error("Error finding node file");
                throw new NotFoundException("Error finding node file with the id of " + datasetId);
            } else if (isGraphPresented == false) {
                logger.error("Error finding graph file");
                throw new NotFoundException("Error finding graph file with the id of " + datasetId);
            }
        }

        boolean isJsonValid = false;
        boolean isGeoserver = false;
        boolean isAsc = false;
        boolean isShp = false;
        boolean isTif = false;
        boolean isJoin = false;

        int file_counter = 0;
        int link_counter = 0;
        int node_counter = 0;
        int graph_counter = 0;
        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMENTER_NAME);
            if (paramName.equals(POST_PARAMENTER_FILE) || paramName.equals(POST_PARAMENTER_FILE_LINK) ||
                paramName.equals(POST_PARAMENTER_FILE_NODE) || paramName.equals(POST_PARAMENTER_FILE_GRAPH)) {
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
                InputStream is = null;
                if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE)) {
                    is = (InputStream) inputs.getFields(paramName).get(file_counter).getValueAs(InputStream.class);
                    file_counter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_LINK)) {
                    is = (InputStream) inputs.getFields(paramName).get(link_counter).getValueAs(InputStream.class);
                    link_counter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_NODE)) {
                    is = (InputStream) inputs.getFields(paramName).get(node_counter).getValueAs(InputStream.class);
                    node_counter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMENTER_FILE_GRAPH)) {
                    is = (InputStream) inputs.getFields(paramName).get(graph_counter).getValueAs(InputStream.class);
                    graph_counter++;
                }

                if (is != null) {
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
                }
            }
        }

        // add link, node, graph file name to dataset
        if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
            Component component = dataset.getComponent();
            Link link = component.getLink();
            Node node = component.getNode();
            Graph graph = component.getGraph();
            link.setFileName(linkFileName);
            node.setFileName(nodeFileName);
            graph.setFileName(graphFileName);
            component.setLink(link);
            component.setNode(node);
            component.setGraph(graph);
            dataset.setComponent(component);
        }

        repository.addDataset(dataset);

        // check if there is a source dataset, if so it will be joined to source dataset
        String sourceDataset = dataset.getSourceDataset();

        // join it if it is a table dataset with source dataset existed
        if (sourceDataset.length() > 0 && format.equalsIgnoreCase("table")) {
            isJoin = true;
            isGeoserver = true;
        }

        List<FileDescriptor> dataFDs = dataset.getFileDescriptors();
        List<File> files = new ArrayList<File>();
        File zipFile = null;
        boolean isShpfile = false;

        if (format.equalsIgnoreCase(FileUtils.FORMAT_SHAPEFILE) || format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
            for (int i = 0; i < dataFDs.size(); i++) {
                FileDescriptor sfd = dataFDs.get(i);
                String shpLoc = FilenameUtils.concat(DATA_REPO_FOLDER, sfd.getDataURL());
                File shpFile = new File(shpLoc);
                files.add(shpFile);
                //get file, if the file is in remote, use http downloader
                String fileExt = FilenameUtils.getExtension(shpLoc);
                if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                    isShpfile = true;
                }
            }
            try {
                // create GUID if there is no GUID in the table
                boolean isGuid = true;
                boolean isLinkGuid = true;
                boolean isNodeGuid = true;
                if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                    isLinkGuid = GeotoolsUtils.createGUIDinShpfile(dataset, files, linkFileName);
                    if (isLinkGuid) {
                        logger.debug("The link shapefile already has guid field");
                    }
                    isNodeGuid = GeotoolsUtils.createGUIDinShpfile(dataset, files, nodeFileName);
                    if (isNodeGuid) {
                        logger.debug("The node shapefile already has guid field");
                    }
                } else {
                    isGuid = GeotoolsUtils.createGUIDinShpfile(dataset, files);
                    if (isGuid) {
                        logger.debug("The shapefile already has guid field");
                    }
                }

            } catch (IOException e) {
                logger.error("Error creating temp directory in guid creation process ", e);
                throw new InternalServerErrorException("Error creating temp directory in guid creation process ", e);
            }
        }
        repository.addDataset(dataset);

        if (enableGeoserver && isGeoserver) {
            if (isJoin) {
                // todo: the join process for the network dataset should be added in here
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
                    if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                        GeoserverUtils.networkDatasetUploadToGeoserver(dataset, repository);
                    } else {
                        GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
                    }
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

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    @ApiOperation(value = "Updates the dataset's JSON associated with a dataset id", notes = "This will not upload file content of the dataset to the server, " +
        "they should be done separately using {id}/files endpoint")
    public Object updateObject(@HeaderParam("X-Credential-Username") String username,
                               @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("id") String datasetId,
                               @ApiParam(value = "JSON representing an input dataset", required = true) @FormDataParam("update") String inDatasetJson) {
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
