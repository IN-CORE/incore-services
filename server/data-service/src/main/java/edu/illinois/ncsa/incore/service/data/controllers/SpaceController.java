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

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.PrivilegeLevel;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.spaces.Metadata;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
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
        description = "IN-CORE Data Service for creating and accessing spaces",
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

@Api(value = "spaces", authorizations = {})

@Path("spaces")
public class SpaceController {
    private static final String SERVICES_URL = Config.getConfigProperties().getProperty("dataservice.url");
    private static final String EARTHQUAKE_URL = "/hazard/api/earthquakes/";
    private static final String TORNADO_URL = "/hazard/api/tornadoes/";
    private static final String HURRICANE_URL = "/hazard/api/hurricaneWindfields/";
    private static final String TSUNAMI_URL = "/hazard/api/tsunamis/";
    private static final String FRAGILITY_URL = "/fragility/api/fragilities/";
    private static final List<String> SPACE_IDENTIFIERS = Arrays.asList("metadata", "privileges", "members");
    private static final List<String> METADATA_IDENTIFIERS = Arrays.asList("name");

    public static final String SPACE_MEMBERS = "members";
    public static final String SPACE_METADATA = "metadata";
    public static final String SPACE_METADATA_NAME = "name";
    public static final String SPACE_PRIVILEGES = "privileges";

    private Logger logger = Logger.getLogger(SpaceController.class);

    @Inject
    private IRepository repository;

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

        if (!isSpaceJsonValid(spaceJson)){
            throw new BadRequestException("JSON contains invalid identifiers");
        }

        String metadata = getMetadataFromSpaceJson(spaceJson);
        String privilegesJson = JsonUtils.extractValueFromJsonString(SPACE_PRIVILEGES, spaceJson);
        List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
        String name = JsonUtils.extractValueFromJsonString(SPACE_METADATA_NAME, metadata);

        if(name.equals("")){
            throw new BadRequestException("A name must be included in metadata");
        }

        Space foundSpace = repository.getSpaceByName(name);
        if (foundSpace == null) {
            Space space = new Space();

            space.setMetadata(new Metadata(name));
            space.setMembers(members);
            space.setPrivileges(Privileges.newWithSingleOwner(username));

            if(!privilegesJson.equals("")){
                addPrivilegesToSpace(space, privilegesJson);
            }

            repository.addSpace(space);

            return space;
        } else{
            throw new BadRequestException("Space already exists.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of all available INCORE Dataset spaces", notes = "Return spaces that the user has privileges to read")
    public List<Space> getSpacesList(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "Dataset Id") @QueryParam("dataset") String datasetId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid username");
        }

        List<Space> spaces = repository.getAllSpaces();
        List<Space> filteredSpaces = new ArrayList<>();

        for(Space space : spaces){
            if (authorizer.canRead(username, space.getPrivileges())) {
                if(datasetId != null){
                    if(space.hasMember(datasetId)){
                        filteredSpaces.add(space);
                    }
                } else {
                    filteredSpaces.add(space);
                }
            }
        }

        if(filteredSpaces.size() == 0 && datasetId != null){
            throw new NotFoundException("No spaces have the dataset " + datasetId);
        }
        return filteredSpaces;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the spaces of a dataset from the Dataset collection")
    public Space getSpaceById(@HeaderParam("X-Credential-Username") String username,
                                  @ApiParam(value = "Space Id from data service", required = true) @PathParam("id") String spaceId) {
        Space space = repository.getSpaceById(spaceId);
        if (space == null) {
            logger.error("Error finding space with the id of " + spaceId);
            throw new NotFoundException("Error finding space with the id of " + spaceId);
        }
        if (!(authorizer.canRead(username, space.getPrivileges()))) {
            throw new ForbiddenException("You are not allowed to add the dataset " + spaceId);
        }

        return space;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a space from the Dataset collection")
    public Space updateSpace(@HeaderParam("X-Credential-Username") String username,
                                @ApiParam(value = "Space Id from data service", required = true) @PathParam("id") String spaceId,
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

        if (space == null) {
            throw new NotFoundException();
        }
        if (!(authorizer.canWrite(username, space.getPrivileges()))) {
            throw new ForbiddenException("You are not allowed to modify the space " + spaceId);
        }

        String metadata = JsonUtils.extractValueFromJsonString(SPACE_METADATA, spaceJson);
        List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
        if(metadata.equals("") && members.size() == 0){
            throw new BadRequestException("Invalid identifiers");
        }
        //TODO: will need more work when metadata contains more than just the name. Move on to using ObjectMappers.
        if (!metadata.equals("")) {
            String name = JsonUtils.extractValueFromJsonString(SPACE_METADATA_NAME, metadata);
            if(name.equals("")){
                throw new BadRequestException("Invalid identifier in metadata");
            }
            if(repository.getSpaceByName(name) == null) {
                space.setMetadata(new Metadata(name));
            } else {
                throw new BadRequestException("New name of space already exists");
            }
        }
        if (members.size() > 0){
            for(String datasetId : members){
                addDatasets(space, username, datasetId);
            }
        }

        space = repository.addSpace(space);

        return space;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/datasets/{datasetId}")
    @ApiOperation(value = "Adds a dataset to a space")
    public Space addDatasetsToSpace(@HeaderParam("X-Credential-Username") String username,
                               @ApiParam(value = "Space Id from data service", required = true) @PathParam("id") String spaceId,
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
                              @ApiParam(value = "Space Id from data service", required = true) @PathParam("id") String spaceId,
                              @ApiParam(value = "JSON representing a privilege block", required = true) @FormDataParam("grant") String privilegesJson) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        Space space = getSpace(spaceId);

        if(!authorizer.canWrite(username, space.getPrivileges())){
            throw new NotAuthorizedException(username + " has not write permissions in " +spaceId);
        }

        if(!addPrivilegesToSpace(space, privilegesJson)){
            throw new BadRequestException("No valid privileges found");
        }

        repository.addSpace(space);

        return space;

    }

    /**
     * Verifies that the metadataJson's identifiers are valid
     * @param metadataJson String representing the metadata json
     * @return Metadata String representation if metadataJson contains valid identifiers
     */
    private String getMetadataFromSpaceJson(String metadataJson){
        String metadata = JsonUtils.extractValueFromJsonString(SPACE_METADATA, metadataJson);
        if(metadata.equals("")){
            throw new BadRequestException("Metadata must be included in space json");
        }
        HashMap<String, Object> metadataMap = JsonUtils.extractMapFromJsonString(metadata);

        Set<String> metadataIdentifiers = metadataMap.keySet();

        if (metadataIdentifiers.stream().allMatch(it -> METADATA_IDENTIFIERS.contains(it))){
            return metadata;
        } else {
            throw new BadRequestException("Metadata identifiers are incorrect");
        }
    }

    /**
     * Verifies that a json's identifiers are within the constrains of the defined space identifiers
     * @param spaceJson
     * @return true if spaceJson is valid
     */
    private boolean isSpaceJsonValid(String spaceJson){
        HashMap<String, Object> spaceMap = JsonUtils.extractMapFromJsonString(spaceJson);

        Set<String> spaceIdentifiers = spaceMap.keySet();

        return spaceIdentifiers.stream().allMatch(it -> SPACE_IDENTIFIERS.contains(it));
    }

    /**
     *
     * @param spaceId
     * @return Space if one was found
     */
    private Space getSpace(String spaceId){
        Space space = repository.getSpaceById(spaceId);
        if (space == null) {
            throw new NotFoundException("Error in finding space with id " + spaceId);
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
    private String getFragilityDataset(String datasetId, String username){
        HttpURLConnection con;
        try {
                URL url = new URL(FRAGILITY_URL + datasetId);
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
     * Extracts user and group privileges from json and adds them to a space.
     * @param space
     * @param inJson
     * @return true if privileges were added successfully
     */
    private boolean addPrivilegesToSpace(Space space, String inJson){
        HashMap<String, Object> privilegesMap = JsonUtils.extractMapFromJsonString(inJson);

        if(privilegesMap == null){
            throw new BadRequestException("Invalid Json");
        }

        Object userPrivileges = privilegesMap.get("userPrivileges");
        Object groupPrivileges = privilegesMap.get("groupPrivileges");

        if(userPrivileges == null && groupPrivileges == null){
            return true;
        }

        Privileges privileges = space.getPrivileges();

        if(userPrivileges != null) {
            HashMap<String, String> userPrivilegesMap = getPrivilegesMap((HashMap<String, Object>) userPrivileges);
            Set<String> users = userPrivilegesMap.keySet();
            for(String user : users){
                privileges.addUserPrivileges(user, PrivilegeLevel.valueOf(userPrivilegesMap.get(user)));
            }
        }
        if(groupPrivileges != null) {
            HashMap<String, String> groupPrivilegesMap = getPrivilegesMap((HashMap<String, Object>) groupPrivileges);
            Set<String> users = groupPrivilegesMap.keySet();
            for(String user : users){
                privileges.addGroupPrivileges(user, PrivilegeLevel.valueOf(groupPrivilegesMap.get(user)));
            }
        }
        return true;
    }

    /**
     * Gets a username to privilege level map. Also verifies if the privilege level provided is valid.
     * @param privileges Privileges map to verify that its privilege levels are valid
     * @return Mapping of usernames and privilege levels
     */
    private HashMap<String, String> getPrivilegesMap(HashMap<String, Object> privileges){
        HashMap<String, String> parsedPrivileges = new HashMap<>();

        Set<String> keys = privileges.keySet();

        for(String key : keys){
            String value = privileges.get(key).toString().toUpperCase();
            try{
                PrivilegeLevel.valueOf(value);
            } catch(Exception e){
                throw new BadRequestException("Invalid privilege level");
            }
            parsedPrivileges.put(key, value);
        }

        return parsedPrivileges;
    }

    /**
     * Add dataset(s) to a space
     * @param space Space to modify
     * @param username username
     * @param datasetId Id of dataset
     * @return True if a dataset was found in any service and added to the space successfully
     */
    private boolean addDatasets(Space space, String username, String datasetId){
        //TODO: SpaceController doesn't have to care about what is adding, so we need to rethink the design to avoid
        // the following conditional branching.
        //get dataset from data-service
        if(repository.getDatasetById(datasetId) != null){
            space.addMember(datasetId);
            repository.addSpace(space);
            return true;
        }

        //get dataset from fragility-service
        if(getFragilityDataset(datasetId, username) != null){
            space.addMember(datasetId);
            repository.addSpace(space);
            return true;
        }

        //get datasets from hazard-service
        List<String> datasets = getHazardDatasets(datasetId, username);

        if(datasets != null && datasets.size() > 0) {
            for (String id : datasets) {
                space.addMember(id);
            }
            repository.addSpace(space);
            return true;
        }
        return false;
    }

}
