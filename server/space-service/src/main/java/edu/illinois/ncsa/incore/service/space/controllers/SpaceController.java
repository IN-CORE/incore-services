package edu.illinois.ncsa.incore.service.space.controllers;
/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.PrivilegeLevel;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.models.SpaceMetadata;
import edu.illinois.ncsa.incore.common.utils.JsonUtils;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by ywkim on 7/26/2017.
 */

@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Space Service for creating and accessing spaces",
        version = "v0.3.0",
        title = "IN-CORE v2 Space Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore2.ncsa.illinois.edu"
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

@Api(value = "spaces", authorizations = {})

@Path("spaces")
public class SpaceController {
    private static final String SERVICES_URL = Config.getConfigProperties().getProperty("dataservice.url");
    private static final String EARTHQUAKE_URL = SERVICES_URL.endsWith("/") ? "hazard/api/earthquakes/" : "/hazard/api/earthquakes/";
    private static final String TORNADO_URL = SERVICES_URL.endsWith("/") ? "hazard/api/tornadoes/" : "/hazard/api/tornadoes/";
    private static final String HURRICANE_URL = SERVICES_URL.endsWith("/") ? "hazard/api/hurricaneWindfields/" : "/hazard/api/hurricaneWindfields/";
    private static final String TSUNAMI_URL = SERVICES_URL.endsWith("/") ? "hazard/api/tsunamis/" : "/hazard/api/tsunamis/";
    private static final String FRAGILITY_URL = SERVICES_URL.endsWith("/") ? "fragility/api/fragilities/" : "/fragility/api/fragilities/";
    private static final String DATA_URL = SERVICES_URL.endsWith("/") ? "data/api/datasets/" : "/data/api/datasets/";

    public static final String SPACE_MEMBERS = "members";
    public static final String SPACE_METADATA = "metadata";

    private Logger logger = Logger.getLogger(SpaceController.class);

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ingest space object as json")
    public Space ingestSpace(@HeaderParam("X-Credential-Username") String username,
                             @ApiParam(value = "JSON representing an input space", required = true) @FormDataParam("space") String spaceJson) {
        if (username == null || !JsonUtils.isJSONValid(spaceJson)) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Space newSpace = objectMapper.readValue(spaceJson, Space.class);

            if(newSpace.getName().equals("")){
                throw new BadRequestException("Invalid name");
            }

            if(spaceRepository.getSpaceByName(newSpace.getName()) == null){
                newSpace.addUserPrivileges(username, PrivilegeLevel.ADMIN);

                //TODO: this should change in the future. The space should not have to care about what it is adding.
                List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
                for (String datasetId : members) {
                    addDatasets(newSpace, username, datasetId);
                }

                return spaceRepository.addSpace(newSpace);

            } else {
                throw new BadRequestException("Space already exists with name " + newSpace.getName());
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid space JSON. " + e.toString());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of all available spaces", notes = "Return spaces that the user has privileges to read. If a datasetId is passed, it will return all spaces that contains it.")
    public List<Space> getSpacesList(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "Dataset Id") @QueryParam("dataset") String datasetId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid username");
        }

        if (datasetId != null) {
            if (!authorizer.canUserReadMember(username, datasetId, spaceRepository.getAllSpaces())) {
                throw new ForbiddenException("User can't access the given dataset");
            }
            List<Space> filteredSpaces = authorizer.getAllSpacesUserCanRead(username, spaceRepository.getAllSpaces());
            List<Space> spacesWithDataset = new ArrayList<>();
            for (Space space: filteredSpaces) {
                if (space.hasMember(datasetId)) {
                    spacesWithDataset.add(space);
                }
            }
            if (spacesWithDataset.size() == 0) {
                throw new NotFoundException("No spaces user has access to contain the dataset");
            }
            return spacesWithDataset;
        }

        List<Space> filteredSpaces = authorizer.getAllSpacesUserCanRead(username, spaceRepository.getAllSpaces());
        if(filteredSpaces.size() == 0) {
            throw new ForbiddenException("User can't access any space");
        }

        return filteredSpaces;


    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a space.")
    public Space getSpaceById(@HeaderParam("X-Credential-Username") String username,
                              @ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId) {
        Space space = getSpace(spaceId);
        if (space == null) throw new NotFoundException();

        if (!(authorizer.canRead(username, space.getPrivileges()))) {
            throw new ForbiddenException(username + " is not authorized to access the space " + spaceId);
        }

        return space;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a space.")
    public Space updateSpace(@HeaderParam("X-Credential-Username") String username,
                             @ApiParam(value = "SpaceOld Id from data service", required = true) @PathParam("id") String spaceId,
                             @ApiParam(value = "JSON representing an input space", required = true) @FormDataParam("space") String spaceJson) {
        if (username == null) {
            logger.error("Credential user name should be provided.");
            throw new BadRequestException("Credential user name should be provided.");
        }

        if (!JsonUtils.isJSONValid(spaceJson)) {
            logger.error("Posted json is not a valid json.");
            throw new BadRequestException("Posted json is not a valid json.");
        }

        Space space = getSpace(spaceId);

        if (!(authorizer.canWrite(username, space.getPrivileges()))) {
            throw new ForbiddenException("You are not allowed to modify the space " + spaceId);
        }

        String metadata = JsonUtils.extractValueFromJsonString(SPACE_METADATA, spaceJson);
        List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
        if(metadata.equals("") && members.size() == 0){
            throw new BadRequestException("Invalid identifiers");
        }
        //TODO: this will need to change once we add more fields to metadata
        if (!metadata.equals("")) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                SpaceMetadata newMetadata = objectMapper.readValue(metadata, SpaceMetadata.class);
                String name = newMetadata.getName();
                if(name.equals("")){
                    throw new BadRequestException("Invalid name in metadata");
                }
                if(spaceRepository.getSpaceByName(name) == null) {
                    space.setMetadata(newMetadata);
                } else {
                    throw new BadRequestException("New name of space already exists");
                }
            } catch (IOException e){
                throw new BadRequestException("Invalid metadata. " + e.toString());
            }
        }
        if (members.size() > 0){
            for(String datasetId : members){
                addDatasets(space, username, datasetId);
            }
        }

        space = spaceRepository.addSpace(space);

        return space;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/datasets/{datasetId}")
    @ApiOperation(value = "Adds a dataset to a space")
    public Space addDatasetsToSpace(@HeaderParam("X-Credential-Username") String username,
                                    @ApiParam(value = "SpaceOld Id from data service", required = true) @PathParam("id") String spaceId,
                                    @ApiParam(value = "Dataset Id from data service", required = true) @PathParam("datasetId") String datasetId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        Space space = getSpace(spaceId);

        if(!authorizer.canWrite(username, space.getPrivileges())){
            throw new NotAuthorizedException(username + " can't modify the space");
        }

        if(addDatasets(space, username, datasetId)){
            return space;
        } else{
            throw new NotFoundException("Could not find dataset " + datasetId);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/grant")
    @ApiOperation(value = "Grants new privileges to a space")
    public Space grantPrivilegesToSpace(@HeaderParam("X-Credential-Username") String username,
                                        @ApiParam(value = "SpaceOld Id from data service", required = true) @PathParam("id") String spaceId,
                                        @ApiParam(value = "JSON representing a privilege block", required = true) @FormDataParam("grant") String privilegesJson) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        Space space = getSpace(spaceId);

        if(!authorizer.canWrite(username, space.getPrivileges())){
            throw new NotAuthorizedException(username + " has not write permissions in " +spaceId);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Privileges privileges = objectMapper.readValue(privilegesJson, Privileges.class);
            space.addPrivileges(privileges);
        } catch (IOException e){
            throw new BadRequestException("Invalid privileges JSON. " + e.toString());
        }

        spaceRepository.addSpace(space);

        return space;

    }

    /**
     * If the space is not found it will throw a 404
     * @param spaceId
     * @return SpaceOld if one was found
     */
    private Space getSpace(String spaceId){
        Space space = spaceRepository.getSpaceById(spaceId);
        if (space == null) {
            throw new NotFoundException("Could not find space with id " + spaceId);
        }
        return space;
    }

    /**
     * Makes an HTTP request to hazard service
     * @param datasetId id of dataset
     * @param username  username
     * @return A list of Json responses
     */
    private List<String> getHazardDatasets(String datasetId, String username){
        //TODO: check if there is a better way of doing this
        HttpURLConnection con;
        try {
            List<URL> urls = new ArrayList<>();
            urls.add(new URL(SERVICES_URL + EARTHQUAKE_URL + datasetId));
            urls.add(new URL(SERVICES_URL + TORNADO_URL + datasetId));
            urls.add(new URL(SERVICES_URL + HURRICANE_URL + datasetId));
            urls.add(new URL(SERVICES_URL + TSUNAMI_URL + datasetId));
            try {
                for(URL url : urls) {
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("X-Credential-Username", username);
                    String content = getContent(con);
                    con.disconnect();
                    if (content != null) {
                        List<String> hazardDatasets = JsonUtils.extractValueListFromJsonString("hazardDatasets", content);
                        List<String> datasets = new ArrayList<>();
                        for(String hazardDataset : hazardDatasets){
                            datasets.add(JsonUtils.extractValueFromJsonString("datasetId", hazardDataset));
                        }
                        //tornado hazard has a different identifier name for the dataset
                        if(hazardDatasets.size() == 0){
                            String tornadoDataset = JsonUtils.extractValueFromJsonString("tornadoDatasetId", content);
                            if(!tornadoDataset.equalsIgnoreCase("")){
                                datasets.add(tornadoDataset);
                            }
                        }
                        datasets.add(datasetId);
                        return datasets;
                    }
                }
            } catch (IOException ex){ }
        } catch (MalformedURLException e){ }
        return null;
    }

    /**
     * Makes an HTTP request to the fragility service
     * @param datasetId Id of dataset
     * @param username username
     * @return Json response of API call
     */
    private String getFragilityDataset(String datasetId, String username) {
        HttpURLConnection con;
        try {
            URL url = new URL(SERVICES_URL + FRAGILITY_URL + datasetId);
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Credential-Username", username);
                return getContent(con);
            } catch (IOException ex){}
        } catch (MalformedURLException e){}
        return null;
    }

    private String getDataDataset(String datasetId, String username) {
        HttpURLConnection con;
        try {
            URL url = new URL(SERVICES_URL + DATA_URL + datasetId);
            String uri = url.toString();
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Credential-Username", username);
                return getContent(con);
            } catch (IOException ex){}
        } catch (MalformedURLException e){}
        return null;
    }

    /**
     * Gets the content of a HTTP request
     * @param con
     * @return
     */
    private String getContent(HttpURLConnection con){
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            StringBuffer content = new StringBuffer();
            String jsonString;
            while ((jsonString = in.readLine()) != null) {
                content.append(jsonString);
            }
            in.close();
            return content.toString();
        }catch (IOException ex){}

        return null;
    }

    /**
     * Add dataset(s) to a space
     * @param space SpaceOld to modify
     * @param username username
     * @param datasetId Id of dataset
     * @return True if a dataset was found in any service and added to the space successfully
     */
    private boolean addDatasets(Space space, String username, String datasetId){
        //TODO: SpaceController doesn't have to care about what is adding, so we need to rethink the design to avoid
        // the following conditional branching.
        //get dataset from data-service
        if(getDataDataset(datasetId, username) != null){
            space.addMember(datasetId);
            spaceRepository.addSpace(space);
            return true;
        }

        //get dataset from fragility-service
        if(getFragilityDataset(datasetId, username) != null){
            space.addMember(datasetId);
            spaceRepository.addSpace(space);
            return true;
        }

        //get datasets from hazard-service
        List<String> datasets = getHazardDatasets(datasetId, username);

        if(datasets != null && datasets.size() > 0) {
            for (String id : datasets) {
                space.addMember(id);
            }
            spaceRepository.addSpace(space);
            return true;
        }
        return false;
    }

}
