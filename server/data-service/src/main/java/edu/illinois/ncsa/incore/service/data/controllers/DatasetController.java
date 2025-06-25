/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *   Diego Calderon (NCSA)
 *******************************************************************************/


package edu.illinois.ncsa.incore.service.data.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.exceptions.CsvValidationException;
import edu.illinois.ncsa.incore.common.HazardConstants;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.common.utils.AllocationUtils;
import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.utils.GeoUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.NetworkData;
import edu.illinois.ncsa.incore.service.data.models.NetworkDataset;
import edu.illinois.ncsa.incore.service.data.models.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.utils.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.api.data.*;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.AttributeTypeBuilder;
import org.glassfish.jersey.media.multipart.*;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.locationtech.jts.geom.Point;

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
import org.json.JSONArray;
import org.json.JSONObject;

import org.geotools.api.filter.FilterFactory;
//import org.geotools.api.filter.FilterFactory2;

import org.geotools.data.DefaultTransaction;
//import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.illinois.ncsa.incore.service.data.utils.CommonUtil.datasetComparator;

/**
 * Created by ywkim on 7/26/2017.
 */

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Data Service for creating and accessing datasets",
        version = "1.28.0",
        title = "IN-CORE v2 Data Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://tools.in-core.org"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    )
)

@Tag(name = "datasets")

@Path("datasets")
public class DatasetController {
    private static final String DATA_REPO_FOLDER = System.getenv("DATA_REPO_DATA_DIR");
    private static final String GEOSERVER_ENABLE = System.getenv("GEOSERVER_ENABLE");
    private static final String PG_HOST = System.getenv("PG_HOST");
    private static final String PG_PORT = System.getenv("PG_PORT"); // string because it's an env var
    private static final String PG_DATABASE = System.getenv("PG_DATABASE");
    private static final String PG_USER = System.getenv("PG_USER");
    private static final String PG_PASSWORD = System.getenv("PG_PASSWORD");
    private static final String POST_PARAMETER_NAME = "name";
    private static final String POST_PARAMETER_FILE = "file";
    private static final String POST_PARAMETER_FILE_LINK = "link-file";
    private static final String POST_PARAMETER_FILE_NODE = "node-file";
    private static final String POST_PARAMETER_FILE_GRAPH = "graph-file";
    private static final String UPDATE_OBJECT_NAME = "property name";
    private static final String UPDATE_OBJECT_VALUE = "property value";
    private static final Logger logger = Logger.getLogger(DatasetController.class);

    private final String username;
    private final List<String> groups;

    @Inject
    private IRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IUserAllocationsRepository allocationsRepository;

    @Inject
    private IUserFinalQuotaRepository quotaRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public DatasetController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
        ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets a dataset from the Dataset collection", description = "")
    public Dataset getDatasetbyId(
        @Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
        Dataset dataset = repository.getDatasetById(datasetId);
        if (dataset == null) {
            logger.error("Error finding dataset with the id of " + datasetId);
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the dataset " + datasetId);
        }

        dataset.setSpaces(spaceRepository.getSpaceNamesOfMember(datasetId));

        //feeling lucky, try to get dataset directly from user's space
        Space space = spaceRepository.getSpaceByName(this.username);
        if (space != null && space.hasMember(datasetId)) {
            return dataset;
        }

        if (authorizer.canUserReadMember(this.username, datasetId, spaceRepository.getAllSpaces(), this.groups)) {
            return dataset;
        }
        throw new IncoreHTTPException(Response.Status.FORBIDDEN,
            username + " does not have the privileges to access the dataset " + datasetId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets a list of datasets", description = "Can filter by type, title, creator etc.")
    public List<Dataset> getDatasets(@Parameter(name = "DataType of IN-CORE datasets. Can filter by partial datatype strings. ex: " +
        "ergo:buildingInventoryVer5, ergo:census", required = false) @QueryParam("type") String typeStr,
                                     @Parameter(name = "Title of dataset. Can filter by partial title strings", required = false) @QueryParam("title") String titleStr,
                                     @Parameter(name = "Username of the creator", required = false) @QueryParam("creator") String creator,
                                     @Parameter(name = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                     @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
                                     @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
                                     @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                     @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit,
                                     @Parameter(name = "Exclusion of the hazard dataset") @DefaultValue("true") @QueryParam("excludeHazard") boolean excludeHazard) {

        // import eq comparator
        Comparator<Dataset> comparator = datasetComparator(sortBy, order);

        List<Dataset> datasets;
        if (typeStr != null && titleStr == null) {  // query only for the type
            datasets = repository.getDatasetByType(typeStr, excludeHazard);
        } else if (typeStr == null && titleStr != null) {   // query only for the title
            datasets = repository.getDatasetByTitle(titleStr, excludeHazard);
        } else if (typeStr != null && titleStr != null) {   // query for both type and title
            datasets = repository.getDatasetByTypeAndTitle(typeStr, titleStr, excludeHazard);
        } else {
            datasets = repository.getAllDatasets(excludeHazard);
        }

        if (datasets == null) {
            logger.error("Error finding dataset");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any datasets");
        }
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the space " + spaceName);
            }
            if (!authorizer.canRead(username, space.getPrivileges(), this.groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            datasets = datasets.stream()
                .filter(hurricane -> spaceMembers.contains(hurricane.getId()))
                .sorted(comparator)
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return datasets;
        }
        //get all datasets that the user can read
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        //return the intersection between all datasets and the ones the user can read
        List<Dataset> accessibleDatasets = datasets.stream()
            .filter(dataset -> userMembersSet.contains(dataset.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleDatasets;
    }

    @GET
    @Path("{id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Returns a zip file that contains all the files attached to a dataset specified by {id}", description = "")
    public Response getFileByDataset(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
        File outFile = null;
        Dataset dataset = getDatasetbyId(datasetId);
        try {
            outFile = FileUtils.loadFileFromService(dataset, repository, false, "");
        } catch (IOException e) {
            logger.error("Error creating temp directory for " + datasetId, e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error creating temp directory for " + datasetId);
        } catch (URISyntaxException e) {
            logger.error("Error creating file with given url for " + datasetId, e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error creating file with given url for " + datasetId);
        }
        if (outFile != null) {
            String fileName = outFile.getName();
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"").build();
        } else {
            logger.error("Error finding output zip file for " + datasetId);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error finding output zip file for " + datasetId);
        }
    }

    @GET
    @Path("{id}/files")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of files associated with the dataset and their metadata", description = "")
    public List<FileDescriptor> getDatasetsFiles(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
        Dataset dataset = getDatasetbyId(datasetId);

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        if (fds == null) {
            logger.error("Error finding FileDescriptor from the dataset with the id of " + datasetId);
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the file descriptors from the dataset " + datasetId);
        }
        return fds;
    }

    @GET
    @Path("{id}/files/{file_id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Returns a file that is attached to a FileDescriptor of a dataset", description = "")
    public Response getFileByFileDescriptor(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String id,
                                            @Parameter(name = "FileDescriptor Object Id", required = true) @PathParam("file_id") String fileId) {
        File outFile = null;
        Dataset dataset = getDatasetbyId(id);

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
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"").build();
        } else {
            logger.error("Error finding output file.");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the output file.");
        }

    }

    @GET
    @Path("{id}/files/{file_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets metadata of a file associated to a dataset", description = "")
    public FileDescriptor getFileByDatasetIdFileDescriptor(@Parameter(name = "Dataset Id from data service", required = true) @PathParam(
        "id") String id,
                                                           @Parameter(name = "FileDescriptor Object Id", required = true) @PathParam(
                                                               "file_id") String fileId) {
        Dataset dataset = getDatasetbyId(id);

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
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the file descriptor " + fileId);
        }
        return fileDescriptor;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Ingest dataset object as json", description = "Files have to uploaded to the dataset separately using {id}/files " +
        "endpoint")
    public Dataset ingestDataset(@Parameter(name = "JSON representing an input dataset", required = true) @FormDataParam("dataset") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        if (isJsonValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid input dataset, please verify that the dataset is a valid " +
                "JSON.");
        }

        boolean isDatasetParameterValid = DataJsonUtils.isDatasetParameterValid(inDatasetJson);
        if (isDatasetParameterValid != true) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Invalid input dataset. Please verify that the dataset does not contain any wrong parameters.");
        }

        if (JsonUtils.extractValueFromJsonString("id", inDatasetJson) != ""){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Ids are auto-generated by the system. " +
                "Setting an id is not allowed");
        }

        boolean isHazardDataset = false;
        boolean postOk = false;

        String title = "";
        String dataType = "";
        String sourceDataset = "";
        String format = "";
        String fileName = "";
        String description = "";

        // create DataWolf POJO object
        Dataset dataset = new Dataset();
        if (isJsonValid) {
            title = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TITLE, inDatasetJson);
            dataType = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_TYPE, inDatasetJson);
            sourceDataset = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_SOURCE_DATASET, inDatasetJson);
            format = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FORMAT, inDatasetJson);
            fileName = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_FILE_NAME, inDatasetJson);
            description = JsonUtils.extractValueFromJsonString(FileUtils.DATASET_DESCRIPTION, inDatasetJson);

            dataset.setTitle(title);
            dataset.setCreator(this.username);
            dataset.setOwner(this.username);
            dataset.setDataType(dataType);
            dataset.setDescription(description);
            dataset.setSourceDataset(sourceDataset);
            dataset.setFormat(format);

            String subDataType;
            if (dataType.contains(":")) {
                // Compare what comes after the name space (e.g. probabilisticEarthquakeRaster from ergo:probabilisticEarthquakeRaster)
                subDataType = dataType.split(":")[1];
            } else {
                subDataType = dataType;
            }

            // check if the dataset is hazard dataset
            isHazardDataset = HazardConstants.DATA_TYPE_HAZARD.stream().anyMatch(s1 -> s1.contains(subDataType));

            if (isHazardDataset) {
                postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "hazardDatasets");
            } else {
                postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "datasets");
            }

            if (!postOk) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    AllocationConstants.DATASET_ALLOCATION_MESSAGE);
            }

            // add network information in the dataset
            if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                NetworkDataset networkDataset = DataJsonUtils.createNetworkDataset(inDatasetJson);
                dataset.setNetworkDataset(networkDataset);
            }

            dataset = repository.addDataset(dataset);
            if (dataset == null) {
                logger.error("Error posting dataset");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add a " +
                    "dataset to the data repository.");
            }

            String id = dataset.getId();

            Space space = spaceRepository.getSpaceByName(this.username);
            if (space == null) {
                space = new Space(this.username);
                space.addMember(id);
                space.setPrivileges(Privileges.newWithSingleOwner(this.username));
            } else {
                space.addMember(id);
            }

            Space updated_space = spaceRepository.addSpace(space);
            if (updated_space == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "There was an unexpected error when trying to add " +
                    "the dataset to user's space.");
            }

            // add dataset in the usage
            if (isHazardDataset) {
                AllocationUtils.increaseUsage(allocationsRepository, username, "hazardDatasets");
            } else {
                AllocationUtils.increaseUsage(allocationsRepository, username, "datasets");
            }
        }

        dataset.setSpaces(spaceRepository.getSpaceNamesOfMember(dataset.getId()));
        return dataset;
    }


    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    @Operation(summary = "Deletes a dataset", description = "Also deletes attached information like files and geoserver layer")
    public Dataset deleteDataset(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId) {
        Dataset dataset = getDatasetbyId(datasetId);
        boolean geoserverUsed = false;
        long fileSize = 0;

        if (dataset == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the dataset " + datasetId);
        }

        String format = dataset.getFormat();
        String sourceDataset = dataset.getSourceDataset();
        // if there is a source dataset, it will be have a geopkg file uploaded to geoserver
        if (sourceDataset.length() > 0 && format.equalsIgnoreCase("table")) {
            geoserverUsed = true;
        }

        Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (this.username.equals(dataset.getOwner()) || isAdmin) {
            // remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(datasetId)) {
                    space.removeMember(datasetId);
                    spaceRepository.addSpace(space);
                }
            }
            // remove dataset
            if (dataset != null) {
                logger.debug("Removing files from dataset " + datasetId);
                // First - remove the files from the dataset
                List<FileDescriptor> fds = dataset.getFileDescriptors();
                if (fds.size() > 0) {
                    for (FileDescriptor fd : fds) {
                        fileSize += fd.getSize();
                        File file = new File(FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL()));
                        FileUtils.deleteTmpDir(file);
                        if (!geoserverUsed) {
                            geoserverUsed = FileUtils.fileUseGeoserver(file.getName(), Boolean.parseBoolean(GEOSERVER_ENABLE));
                        }
                    }
                }

                // Files removed - delete the dataset entry
                logger.debug("Deleting dataset " + datasetId);
                dataset = repository.deleteDataset(datasetId);

                // Adjust user quota after removing the dataset and files
                // check if the dataset is hazard dataset
                String dataType = dataset.getDataType();
                String subDataType;
                if (dataType.contains(":")) {
                    // Compare what comes after the name space (e.g. probabilisticEarthquakeRaster from ergo:probabilisticEarthquakeRaster)
                    subDataType = dataType.split(":")[1];
                } else {
                    subDataType = dataType;
                }
                boolean isHazardDataset = HazardConstants.DATA_TYPE_HAZARD.stream().anyMatch(s1 -> s1.contains(subDataType));

                // reduce the number of hazard from the space
                if (isHazardDataset) {
                    logger.debug("Decreasing hazard dataset quota");
                    AllocationUtils.decreaseUsage(allocationsRepository, this.username, "hazardDatasets");
                } else {
                    logger.debug("Decreasing dataset quota");
                    AllocationUtils.decreaseUsage(allocationsRepository, this.username, "datasets");
                }

                // decrease file size to usage
                UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
                AllocationUtils.decreaseDatasetFileSize(allocation, allocationsRepository, fileSize, isHazardDataset);

                // remove geoserver layer
                if (geoserverUsed) {
                    boolean isRemoved = GeoserverUtils.removeStoreFromGeoserver(datasetId);
                }
            }
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                this.username + " is not authorized to delete the dataset " + datasetId);
        }


        return dataset;

    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/files")
    @Operation(summary = "Upload file(s) to attach to a dataset", description = "GIS files like shp, tif etc. are also uploaded to IN-CORE " +
        "geoserver")
    public Dataset uploadFiles(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId,
                               @Parameter(name = "Form inputs representing the file(s). The id/key of each input file has to be 'file'",
                                   required = true)
                               FormDataMultiPart inputs) throws IOException {
        if (!authorizer.canUserWriteMember(this.username, datasetId, spaceRepository.getAllSpaces(), this.groups)) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                this.username + " has no permission to modify the dataset " + datasetId);
        }
        // adding geoserver flag
        // if this flas is false, the data will not be uploaded to geoserver
        boolean enableGeoserver = GEOSERVER_ENABLE.equalsIgnoreCase("true");

        boolean isHazardDataset = false;
        boolean postOk = false;

        int bodyPartSize = inputs.getBodyParts().size();
        String objIdStr = datasetId;
        String paramName = "";
        Dataset dataset = getDatasetbyId(objIdStr);

        // check if the dataset is hazard dataset
        String dataType = dataset.getDataType();
        String subDataType;
        if (dataType.contains(":")) {
            // Compare what comes after the name space (e.g. probabilisticEarthquakeRaster from ergo:probabilisticEarthquakeRaster)
            subDataType = dataType.split(":")[1];
        } else {
            subDataType = dataType;
        }
        isHazardDataset = HazardConstants.DATA_TYPE_HAZARD.stream().anyMatch(s1 -> s1.contains(subDataType));

        long fileSize = 0;

        if (isHazardDataset) {
            postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "hazardDatasetSize");
        } else {
            postOk = AllocationUtils.canCreateAnyDataset(allocationsRepository, quotaRepository, username, "datasetSize");
        }

        if (!postOk) {
            if (isHazardDataset) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    AllocationConstants.HAZARD_DATASET_ALLOCATION_FILESIZE_MESSAGE);
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                    AllocationConstants.DATASET_ALLOCATION_FILESIZE_MESSAGE);
            }
        }

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
                paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMETER_NAME);
                if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_LINK)) {
                    String tmpName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    String fileExt = FilenameUtils.getExtension(tmpName);
                    if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                        isLinkPresented = true;
                        linkFileName = tmpName;
                    }
                } else if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_NODE)) {
                    String tmpName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    String fileExt = FilenameUtils.getExtension(tmpName);
                    if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                        isNodePresented = true;
                        nodeFileName = tmpName;
                    }
                } else if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_GRAPH)) {
                    graphFileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                    isGraphPresented = true;
                }
            }

            if (isLinkPresented == false) {
                logger.error("Error finding link file");
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Error finding link file with the id of " + datasetId);
            } else if (isNodePresented == false) {
                logger.error("Error finding node file");
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Error finding node file with the id of " + datasetId);
            } else if (isGraphPresented == false) {
                logger.error("Error finding graph file");
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Error finding graph file with the id of " + datasetId);
            }
        }

        boolean isGeoserver = false;
        boolean isAsc = false;
        boolean isShp = false;
        boolean isTif = false;
        boolean isZip = false;
        boolean isJoin = false;
        boolean isPrj = false;
        boolean isGpkg = false;

        int fileCounter = 0;
        int linkCounter = 0;
        int nodeCounter = 0;
        int graphCounter = 0;

        File savedZipFile = null;
        String tempDir = null;

        for (int i = 0; i < bodyPartSize; i++) {
            paramName = inputs.getBodyParts().get(i).getContentDisposition().getParameters().get(POST_PARAMETER_NAME);
            if (paramName.equals(POST_PARAMETER_FILE) || paramName.equals(POST_PARAMETER_FILE_LINK) ||
                paramName.equals(POST_PARAMETER_FILE_NODE) || paramName.equals(POST_PARAMETER_FILE_GRAPH)) {
                String fileName = inputs.getBodyParts().get(i).getContentDisposition().getFileName();
                String fileExt = FilenameUtils.getExtension(fileName);
                if (FileUtils.fileUseGeoserver(fileName, enableGeoserver)) {
                    isGeoserver = true;
                }
                if (fileExt.equalsIgnoreCase("asc")) {
                    isAsc = true;
                } else if (fileExt.equalsIgnoreCase("tif")) {
                    isTif = true;
                } else if (fileExt.equalsIgnoreCase("shp")) {
                    isShp = true;
                } else if (fileExt.equalsIgnoreCase("zip")) {
                    isZip = true;
                } else if (fileExt.equalsIgnoreCase("prj")) {
                    isPrj = true;
                } else if (fileExt.equalsIgnoreCase("gpkg")) {
                    isGpkg = true;
                }

                // process zip file
                if (isZip) {
                    // TODO: we need to decide the logic about uploading zip file.
                    // for now, the following logic will be applied in handling zip file uploading
                    // when uploading zip file, it should be only one file (zip file) uploaded
                    // the zip file uploaded should be the zipped shapefile
                    // the zip file should contain all the shapefile components (shp, shx, dbf, prj)
                    // the dataset should not have any FileDescriptor entry

                    // check how many files are uploaded, if it is more than one file, then raise an error
                    if (bodyPartSize > 1) {
                        logger.error("There should be only one file uploaded when it comes with zip file ");
                        throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                            "There are more than one file uploaded with zip file. " +
                                "Please upload only single zip file.");
                    }
                }

                // process geopackage file
                if (isGpkg) {
                    // if the file is geopackage but the format is shapefile, it should return error
                    if (!format.equalsIgnoreCase(FileUtils.FORMAT_GEOPACKAGE)) {
                        logger.error("The attached file is geopackage while dataset's format is no geopackage.");
                        throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                            "The attached file is geopackage but dataset's format is not geopackage.");
                    }

                    // check how many files are uploaded, if it is more than one file, then raise an error
                    if (bodyPartSize > 1) {
                        logger.error("There should be only one file uploaded when it comes with geopackage file ");
                        throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                            "There are more than one file uploaded with geopackage dataset. " +
                                "Please upload only single geopackage file.");
                    }
                }

                InputStream is = null;
                if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE)) {
                    is = inputs.getFields(paramName).get(fileCounter).getValueAs(InputStream.class);
                    fileCounter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_LINK)) {
                    is = inputs.getFields(paramName).get(linkCounter).getValueAs(InputStream.class);
                    linkCounter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_NODE)) {
                    is = inputs.getFields(paramName).get(nodeCounter).getValueAs(InputStream.class);
                    nodeCounter++;
                } else if (paramName.equalsIgnoreCase(POST_PARAMETER_FILE_GRAPH)) {
                    is = inputs.getFields(paramName).get(graphCounter).getValueAs(InputStream.class);
                    graphCounter++;
                }

                if (is != null) {
                    if (format.equalsIgnoreCase(FileUtils.FORMAT_SHAPEFILE) && isZip) {
                        // try saving zip file in temp directory
                        try {
                            // create tempDir
                            tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();

                            // create the output file
                            savedZipFile = new File(tempDir, fileName);
                            if (!savedZipFile.getParentFile().isDirectory()) {
                                new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error storing zip file");
                            }

                            // Write the file to disk
                            try (FileOutputStream fos = new FileOutputStream(savedZipFile)) {
                                byte[] buf = new byte[10240];
                                int len = 0;
                                while ((len = is.read(buf)) >= 0) {
                                    fos.write(buf, 0, len);
                                }
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            logger.error("Error storing files of the dataset with the id of " + datasetId);
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error storing files of the dataset with" +
                                " the id of " + datasetId);
                        }
                    } else {
                        FileDescriptor fd = new FileDescriptor();
                        FileStorageDisk fsDisk = new FileStorageDisk();

                        fsDisk.setFolder(DATA_REPO_FOLDER);
                        try {
                            fd = fsDisk.storeFile(fileName, is);
                            fd.setFilename(fileName);
                        } catch (IOException e) {
                            logger.error("Error storing files of the dataset with the id of " + datasetId);
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error storing files of the dataset with" +
                                " the id of " + datasetId);
                        }
                        dataset.addFileDescriptor(fd);

                        // add file size
                        fileSize += fd.getSize();
                    }
                }
            }
        }

        // add link, node, graph file name to dataset
        if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
            NetworkDataset networkDataset = dataset.getNetworkDataset();
            NetworkData link = networkDataset.getLink();
            NetworkData node = networkDataset.getNode();
            NetworkData graph = networkDataset.getGraph();
            link.setFileName(linkFileName);
            node.setFileName(nodeFileName);
            graph.setFileName(graphFileName);
            networkDataset.setLink(link);
            networkDataset.setNode(node);
            networkDataset.setGraph(graph);
            dataset.setNetworkDataset(networkDataset);
        }

        // check if there is a source dataset, if so it will be joined to source dataset
        String sourceDataset = dataset.getSourceDataset();

        // join it if it is a table dataset with source dataset existed
        if (sourceDataset.length() > 0 && format.equalsIgnoreCase("table")) {
            isJoin = true;
            isGeoserver = true;
        }

        List<FileDescriptor> dataFDs = dataset.getFileDescriptors();
        List<File> files = new ArrayList<File>();

        File geoPkgFile = null;

        if (format.equalsIgnoreCase(FileUtils.FORMAT_SHAPEFILE) || format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
            for (int i = 0; i < dataFDs.size(); i++) {
                FileDescriptor sfd = dataFDs.get(i);
                String shpLoc = FilenameUtils.concat(DATA_REPO_FOLDER, sfd.getDataURL());
                File shpFile = new File(shpLoc);
                files.add(shpFile);
                //get file, if the file is in remote, use http downloader
                String fileExt = FilenameUtils.getExtension(shpLoc);
                if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                    isShp = true;
                } else if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_PRJ)) {
                    isPrj = true;
                }
            }
            try {
                if (isZip) {
                    // when it is zip file the dataset should not have any files attached yet.
                    // todo: check if it is okay to add zip file with the datset that already has files
                    //  if so, modify and add the code for handling that
                    if (files.size() > 0) {
                        // remove temp directory used for unzip
                        FileUtils.deleteTmpDir(savedZipFile);
                        logger.error("The dataset should not have any files when uploading zip file ");
                        throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                            "The dataset already has file(s). " +
                                "Uploading zip files is only allowed for datasets with no files.");
                    }

                    // create temp dir and copy zip files to temp dir
//                    String tempDir = Files.createTempDirectory(FileUtils.DATA_TEMP_DIR_PREFIX).toString();
                    List<File> copiedFileList = GeotoolsUtils.performUnzipShpFile(savedZipFile, tempDir);

                    // unzip zip file and add to file descriptor
                    if (copiedFileList != null) {
                        for (File shpFile : copiedFileList) {
                            FileDescriptor fd = new FileDescriptor();
                            FileStorageDisk fsDisk = new FileStorageDisk();

                            fsDisk.setFolder(DATA_REPO_FOLDER);
                            try {
                                InputStream is = new FileInputStream(shpFile);
                                String fileName = shpFile.getName();
                                String fileExt = FilenameUtils.getExtension(shpFile.getName());
                                if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_PRJ)) {
                                    isPrj = true;
                                } else if (fileExt.equalsIgnoreCase(FileUtils.EXTENSION_SHP)) {
                                    isShp = true;
                                }
                                fd = fsDisk.storeFile(fileName, is);
                                fd.setFilename(fileName);
                            } catch (IOException e) {
                                logger.error("Error storing files of the dataset with the id of " + datasetId);
                                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error storing files of the dataset " +
                                    "with the id of " + datasetId);
                            }
                            dataset.addFileDescriptor(fd);
                        }
                        // after adding files to FileDescriptor, remove tempDir
                        FileUtils.deleteTmpDir(copiedFileList.get(0));
                    } else {
                        logger.debug("Unzipping zip file failed");
                        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Unzipping zip file failed.");
                    }

                    // create and add FileDescriptor for unzipped shapefiles
                    List<FileDescriptor> unzippedFDs = dataset.getFileDescriptors();
                    List<File> unzippedFiles = new ArrayList<File>();
                    for (int i = 0; i < unzippedFDs.size(); i++) {
                        FileDescriptor sfd = unzippedFDs.get(i);
                        String shpLoc = FilenameUtils.concat(DATA_REPO_FOLDER, sfd.getDataURL());
                        File shpFile = new File(shpLoc);
                        unzippedFiles.add(shpFile);
                    }

                    // check if unzipped shapefile has GUID
                    if (!GeotoolsUtils.isGUIDinShpfile(dataset, unzippedFiles)) {
                        FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                        logger.debug("The shapefile does not have guid field.");
                        throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "No GUID field.");
                    }
                } else {
                    // check if GUID is in the input shapefile
                    if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                        if (!GeotoolsUtils.isGUIDinShpfile(dataset, files, linkFileName) ||
                            !GeotoolsUtils.isGUIDinShpfile(dataset, files, nodeFileName)) {
                            FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                            logger.debug("The shapefile does not have guid field.");
                            throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "No GUID field.");
                        }
                    } else {
                        if (!GeotoolsUtils.isGUIDinShpfile(dataset, files)) {
                            FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                            logger.debug("The shapefile does not have guid field.");
                            throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "No GUID field.");
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("The shapefile does not have guid field or not a complete shapefile ", e);
                throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "The shapefile does not have guid field " +
                    "or not a complete shapefile. Please check the shapefile.");
            }
        }

        SimpleFeatureCollection sfc = null; // this will be used for uploading geopackage to geoserver
        if (format.equalsIgnoreCase(FileUtils.FORMAT_GEOPACKAGE)) {
            if (!isGpkg) {
                FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                logger.debug("The given file is not a geopackage file.");
                throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "Give file is not a geopackage file.");
            }
            File tmpFile = new File(FilenameUtils.concat(DATA_REPO_FOLDER, dataFDs.get(0).getDataURL()));

            // check if geopackage only has a single layer
            // and the layer name is the same as file name
            // and it is not the raster data since incore-service doesn't support it yet
            GeoUtils.gpkgValidationResult isGpkgFit = GeotoolsUtils.isGpkgFitToService(tmpFile);
            if (isGpkgFit != GeoUtils.gpkgValidationResult.VALID) {
                FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                if (isGpkgFit == GeoUtils.gpkgValidationResult.RASTER_OR_NO_VECTOR_LAYER) {
                    logger.debug("The geopackage has not vector layer or contains the raster layer that is not being supported yet.");
                    throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                        "The geopackage has no vector layer or contains the raster layer that is not being supported yet.");
                } else if (isGpkgFit == GeoUtils.gpkgValidationResult.MULTIPLE_VECTOR_LAYERS) {
                    logger.debug("The geopackage has to have a single layer.");
                    throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                        "The geopackage has to have a single layer.");
                } else if (isGpkgFit == GeoUtils.gpkgValidationResult.NAME_MISMATCH) {
                    logger.debug("The geopackage's'layer name should be the same as file name.");
                    throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE,
                        "The geopackage's'layer name should be the same as file name.");
                }
            }

            // check if geopackage has guid
            sfc = GeotoolsUtils.getSimpleFeatureCollectionFromGeopackage(tmpFile);
            if (!GeotoolsUtils.isGUIDinGeopackage(sfc)) {
                FileUtils.removeFilesFromFileDescriptor(dataset.getFileDescriptors());
                logger.debug("The geopackage does not have guid field.");
                throw new IncoreHTTPException(Response.Status.NOT_ACCEPTABLE, "No GUID field.");
            }
        }

        // TODO: This a patch/hotfix so space is not saved when updating the dataset.
        //  May be this endpoint should not try to addDataset, rather it should just try to update the files section of the existing dataset
        dataset.setSpaces(null);
        repository.addDataset(dataset);

        // add file size to usage
        UserAllocations allocation = allocationsRepository.getAllocationByUsername(username);
        AllocationUtils.increaseDatasetFileSize(allocation, allocationsRepository, fileSize, isHazardDataset);

        if (enableGeoserver && isGeoserver) {
            if (isJoin) {
                File joinedShapefile = null;
                // todo: the join process for the network dataset should be added in here.
                try {
                    //joinedShapefile = FileUtils.joinShpTable(dataset, repository, true);
                    geoPkgFile = FileUtils.joinShpTable(dataset, repository, true);
                    if (!GeoserverUtils.uploadGpkgToGeoserver(dataset.getId(), geoPkgFile)) {
                        logger.error("Fail to upload geopackage file");
                        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Fail to upload geopakcage file.");
                    }
//                     GeoserverUtils.uploadShpZipToGeoserver(dataset.getId(), zipFile);
                } catch (IOException e) {
                    logger.error("Error making temp directory in joining process ", e);
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error making temp directory in joining process.");
                } catch (IncoreHTTPException e) {
                    logger.error(e.getMessage());
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
                } catch (CsvValidationException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
                // clean up
                FileUtils.deleteTmpDir(geoPkgFile);
            } else {
                try {
                    if (format.equalsIgnoreCase(FileUtils.FORMAT_NETWORK)) {
                        if (isShp && isPrj) {
                            GeoserverUtils.networkDatasetUploadToGeoserver(dataset, repository);
                        } else {
                            logger.error("There is no prj file. Uploading to geoserver has been canceled");
                        }
                    } else if (format.equalsIgnoreCase("raster") || format.equalsIgnoreCase("geotiff") ||
                        format.equalsIgnoreCase("tif") || format.equalsIgnoreCase("tiff")) {
                        GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
                    } else if (format.equalsIgnoreCase(FileUtils.FORMAT_GEOPACKAGE)) {
                        double[] bbox = GeotoolsUtils.getBboxFromGeopackage(sfc);
                        dataset.setBoundingBox(bbox);
                        repository.addDataset(dataset);
                        // uploading geoserver must involve the process of renaming the database in geopackage
                        File gpkgFile = new File(FilenameUtils.concat(DATA_REPO_FOLDER, dataFDs.get(0).getDataURL()));
                        if (!GeoserverUtils.uploadGpkgToGeoserver(dataset.getId(), gpkgFile)) {
                            logger.error("Fail to upload geopackage file");
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Fail to upload geopakcage file.");
                        }
                    } else {
                        if (isShp && isPrj) {
                            GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, isTif, isAsc);
                        } else {
                            logger.error("There is no prj file. Uploading to geoserver has been canceled");
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error uploading dataset to geoserver ", e);
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading dataset to geoserver.");
                } catch (URISyntaxException e) {
                    logger.error("Error making file using dataset's location url ", e);
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Error making file using dataset's location uri.");
                }
            }
        }
        return dataset;
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    @Operation(summary = "Updates the dataset's JSON associated with a dataset id", description = "Only allows updating string attributes " +
        "of the dataset. This will not upload file content of the dataset to the server, they should be done separately using " +
        "{id}/files endpoint")
    public Object updateObject(@Parameter(name = "Dataset Id from data service", required = true) @PathParam("id") String datasetId,
                               @Parameter(name = "JSON representing an input dataset", required = true) @FormDataParam("update") String inDatasetJson) {
        boolean isJsonValid = JsonUtils.isJSONValid(inDatasetJson);
        if (!isJsonValid) {
            logger.error("Invalid json provided.");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid input dataset, please verify that the dataset is a valid " +
                "JSON.");
        }

        Dataset dataset = repository.getDatasetById(datasetId);

        if (dataset == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a dataset with id " + datasetId);
        }

        Boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(dataset.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN,
                this.username + " has no permission to modify the dataset " + datasetId);
        }

        String propName = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_NAME, inDatasetJson);
        String propVal = JsonUtils.extractValueFromJsonString(UPDATE_OBJECT_VALUE, inDatasetJson);

        // Check if the field exists and is of type String
        try {
            Field f = dataset.getClass().getDeclaredField(propName); // Get the passed field from Dataset class
            f.setAccessible(true);
            if (!f.getType().isAssignableFrom(String.class)) {  // check if the dataset field passed accepts strings
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The field provided in " + UPDATE_OBJECT_NAME
                    + " is not a string. This method only allows updating properties of string type.");
            }
        } catch (NoSuchFieldException e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The field provided in "
                + UPDATE_OBJECT_NAME + " does not exist in the dataset. ");
        }

        // Handle special logic for sourceDataset update (i.e., setting parent ID)
        if ("sourceDataset".equalsIgnoreCase(propName)) {
            Dataset parentDataset = repository.getDatasetById(propVal);
            if (parentDataset == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Parent dataset not found: " + propVal);
            }

            // Check if the parent dataset is compatible with the child dataset for join
            String childFormat = dataset.getFormat();
            String parentFormat = parentDataset.getFormat();

            boolean isChildCompatible = true;
            boolean isParentCompatible = true;
            if (childFormat == null || !childFormat.equalsIgnoreCase("table")) {
                isChildCompatible = false;
            }
            if ( parentDataset.getFormat() == null || !parentDataset.getFormat().equalsIgnoreCase("shapefile")) {
                isParentCompatible = false;
            }

            // check compatibility of the datasets by GUID
            boolean isGuidCompatible = ServiceUtils.validateJoinCompatibilityByGuid(dataset, parentDataset, repository);

            dataset.setSourceDataset(propVal);
            Dataset updatedDataset = repository.updateDataset(datasetId, "sourceDataset", propVal);

            // GeoServer logic (only if format is 'table')
            if ( isChildCompatible && isParentCompatible && isGuidCompatible) {
                String format = updatedDataset.getFormat();
                if (format != null && format.equalsIgnoreCase("table")) {
                    try {
                        File geoPkgFile = FileUtils.joinShpTable(updatedDataset, repository, true);
                        boolean success = GeoserverUtils.uploadGpkgToGeoserver(updatedDataset.getId(), geoPkgFile);
                        if (!success) {
                            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "GeoServer upload failed.");
                        }
                        FileUtils.deleteTmpDir(geoPkgFile); // Clean up
                        logger.info("Dataset joined to source dataset and uploaded to geoserver.");
                    } catch (Exception e) {
                        logger.error("Error during GeoServer upload process: " + e.getMessage(), e);
                        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "GeoServer layer creation failed.");
                    }
                }
            } else {
                logger.info("Dataset not joined to parent dataset.");
            }
            return updatedDataset;
        } else {
            dataset = repository.updateDataset(datasetId, propName, propVal);
            return dataset;
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Search for a text in all datasets", description = "Gets all datasets that contain a specific text")
    public List<Dataset> findDatasets(@Parameter(name = "Text to search by", example = "building") @QueryParam("text") String text,
                                      @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("date") @QueryParam("sortBy") String sortBy,
                                      @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("desc") @QueryParam("order") String order,
                                      @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                      @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit,
                                      @Parameter(name = "Exclusion of the hazard dataset") @DefaultValue("true") @QueryParam("excludeHazard") boolean excludeHazard) {
        // import eq comparator
        Comparator<Dataset> comparator = datasetComparator(sortBy, order);

        List<Dataset> datasets;

        Dataset ds = repository.getDatasetById(text);
        if (ds != null) {
            datasets = new ArrayList<Dataset>() {{
                add(ds);
            }};
        } else {
            datasets = this.repository.searchDatasets(text, excludeHazard);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces(), this.groups);

        datasets = datasets.stream()
            .filter(dataset -> membersSet.contains(dataset.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return datasets;
    }

    @POST
    @Path("nsi/bldg-inventory/blob")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(
        summary = "Export features from PostGIS by 5-digit FIPS code",
        description = "Generates a zipped shapefile from PostGIS for given FIPS codes."
    )
    public Response exportShapefileByFips(List<String> fipsCodes) {
        File tempDir = null;
        File zipFile = null;

        try {
            // Generate unique base name
            String baseName = "nsi_building_inventory_" + UUID.randomUUID().toString().replace("-", "");

            // Create temp directory
            tempDir = Files.createTempDirectory("shapefile_export_").toFile();

            // Connect to PostGIS
            DataStore dataStore = GeotoolsUtils.connectToPostGIS(
                PG_HOST, PG_PORT, PG_DATABASE, PG_USER, PG_PASSWORD
            );

            // Build FIPS filter and fetch features
            Filter filter = GeotoolsUtils.buildFipsFilter(fipsCodes);
            SimpleFeatureSource featureSource = dataStore.getFeatureSource("nsi_export_view");
            SimpleFeatureCollection features = featureSource.getFeatures(filter);

            // Build regulated schema and write shapefile
            File shpFile = new File(tempDir, baseName + ".shp");
            SimpleFeatureType schema = GeotoolsUtils.createRegulatedSchema(features.getSchema(), baseName);
            GeotoolsUtils.writeFeaturesToShapefile(features, schema, shpFile);

            // Zip shapefile
            zipFile = new File(tempDir.getParent(), baseName + ".zip");
            GeotoolsUtils.zipDirectory(tempDir, baseName);

            return Response.ok(zipFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"")
                .build();

        } catch (Exception e) {
            logger.error("Failed to export shapefile", e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR,
                "Error exporting shapefile: " + e.getMessage());
        } finally {
            GeotoolsUtils.cleanupDirectory(tempDir);
        }
    }

    @POST
    @Path("/tools/bldg-inventory")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Generate NSI building inventory dataset from FIPS codes", description = "Creates a dataset and uploads a shapefile based on provided FIPS list")
    public Dataset createDatasetFromFipsExport(
        @Parameter(name = "JSON representing an input dataset", required = true)
        @FormDataParam("dataset") String inDatasetJson) {

        // Validate input JSON
        if (!JsonUtils.isJSONValid(inDatasetJson)) {
            logger.error("Posted json is not a valid json.");
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid input dataset, please verify that the dataset is a valid JSON.");
        }

        if (!JsonUtils.extractValueFromJsonString("id", inDatasetJson).isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Ids are auto-generated by the system. Setting an id is not allowed.");
        }

        File tempDir = null;

        try {
            // Extract fields
            String title = JsonUtils.extractValueFromJsonString("title", inDatasetJson);
            String description = JsonUtils.extractValueFromJsonString("description", inDatasetJson);
            List<String> fipsList = JsonUtils.extractValueListFromJsonString("fips_list", inDatasetJson);

            // Generate filenames and temp directory
            String baseName = "nsi_building_inventory_" + UUID.randomUUID().toString().replace("-", "");
            tempDir = Files.createTempDirectory("shp_").toFile();

            // Query PostGIS and write shapefile components
            DataStore dataStore = GeotoolsUtils.connectToPostGIS(PG_HOST, PG_PORT, PG_DATABASE, PG_USER, PG_PASSWORD);
            SimpleFeatureSource featureSource = dataStore.getFeatureSource("nsi_export_view");
            Filter filter = GeotoolsUtils.buildFipsFilter(fipsList);
            SimpleFeatureCollection features = featureSource.getFeatures(filter);
            SimpleFeatureType schema = GeotoolsUtils.createRegulatedSchema(featureSource.getSchema(), baseName);
            File shpFile = new File(tempDir, baseName + ".shp");
            GeotoolsUtils.writeFeaturesToShapefile(features, schema, shpFile);

            // Create dataset object
            Dataset dataset = new Dataset();
            dataset.setTitle(title);
            dataset.setDescription(description);
            dataset.setCreator(this.username);
            dataset.setOwner(this.username);
            dataset.setDataType("ergo:buildingInventoryVer6");
            dataset.setFormat("shapefile");

            dataset = repository.addDataset(dataset);
            if (dataset == null) {
                logger.error("Failed to create dataset.");
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create dataset.");
            }

            // Add dataset to user space
            String id = dataset.getId();
            Space space = spaceRepository.getSpaceByName(this.username);
            if (space == null) {
                space = new Space(this.username);
                space.addMember(id);
                space.setPrivileges(Privileges.newWithSingleOwner(this.username));
            } else {
                space.addMember(id);
            }

            Space updatedSpace = spaceRepository.addSpace(space);
            if (updatedSpace == null) {
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to update user's space.");
            }

            // Upload each shapefile component directly
            for (String ext : List.of("shp", "shx", "dbf", "prj")) {
                File component = new File(tempDir, baseName + "." + ext);
                if (component.exists()) {
                    InputStream is = new FileInputStream(component);
                    FileDescriptor fd;
                    FileStorageDisk fsDisk = new FileStorageDisk();
                    fsDisk.setFolder(DATA_REPO_FOLDER);
                    fd = fsDisk.storeFile(component.getName(), is);
                    fd.setFilename(component.getName());
                    dataset.addFileDescriptor(fd);
                }
            }

            repository.addDataset(dataset);

            // Return enriched dataset
            dataset.setSpaces(spaceRepository.getSpaceNamesOfMember(dataset.getId()));

            // === Optional GeoServer Upload ===
            boolean enableGeoserver = GEOSERVER_ENABLE.equalsIgnoreCase("true");
            boolean isShp = true;
            boolean isPrj = false;

            // Check if the dataset has required shapefile components
            for (FileDescriptor fd : dataset.getFileDescriptors()) {
                String filename = fd.getFilename().toLowerCase();
                if (filename.endsWith(".prj")) {
                    isPrj = true;
                }
            }

            if (enableGeoserver && isShp && isPrj) {
                try {
                    GeoserverUtils.datasetUploadToGeoserver(dataset, repository, isShp, false, false);
                } catch (IOException | URISyntaxException e) {
                    logger.error("Error uploading dataset to GeoServer: " + dataset.getId(), e);
                    throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "GeoServer upload failed for dataset: " + dataset.getId());
                }
            } else {
                logger.info("GeoServer upload skipped (GEOSERVER_ENABLE=false or missing .prj file).");
            }


            return dataset;

        } catch (Exception e) {
            logger.error("Error while creating dataset from FIPS export", e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            // Cleanup shapefile components
            if (tempDir != null && tempDir.exists()) {
                GeotoolsUtils.cleanupDirectory(tempDir);
            }
        }
    }






}
