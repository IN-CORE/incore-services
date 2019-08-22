package edu.illinois.ncsa.incore.service.space.controllers;
/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************
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
import edu.illinois.ncsa.incore.service.space.models.Members;
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
        version = "v0.4.0",
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

    private static final String SPACE_MEMBERS = "members";
    private static final String SPACE_METADATA = "metadata";

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

            if (newSpace.getName().equals("")) {
                throw new BadRequestException("Invalid name");
            }

            if (spaceRepository.getSpaceByName(newSpace.getName()) == null) {
                newSpace.addUserPrivileges(username, PrivilegeLevel.ADMIN);

                //TODO: this should change in the future. The space should not have to care about what it is adding.
                List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
                for (String id : members) {
                    addMembers(newSpace, username, id);
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
    @ApiOperation(value = "Gets the list of all available spaces", notes = "Return spaces that the user has read/write/admin privileges on. If a member Id is passed, it will return all spaces that contains the member.")
    public List<Space> getSpacesList(@HeaderParam("X-Credential-Username") String username,
                                     @ApiParam(value = "Member Id") @QueryParam("member") String memberId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid username");
        }

        if (memberId != null) {
            if (!authorizer.canUserReadMember(username, memberId, spaceRepository.getAllSpaces())) {
                throw new ForbiddenException("User can't access the given member");
            }
            List<Space> filteredSpaces = authorizer.getAllSpacesUserCanRead(username, spaceRepository.getAllSpaces());
            List<Space> spacesWithMember = new ArrayList<>();
            for (Space space : filteredSpaces) {
                if (space.hasMember(memberId)) {
                    spacesWithMember.add(space);
                }
            }
            if (spacesWithMember.size() == 0) {
                throw new NotFoundException("No spaces user has access to contain the member with id " + memberId);
            }
            return spacesWithMember;
        }

        List<Space> filteredSpaces = authorizer.getAllSpacesUserCanRead(username, spaceRepository.getAllSpaces());
        if (filteredSpaces.size() == 0) {
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
                             @ApiParam(value = "Space id.", required = true) @PathParam("id") String spaceId,
                             @ApiParam(value = "JSON representing a space") @FormDataParam("space") String spaceJson,
                             @ApiParam(value = "JSON representing a members list for removing from space") @FormDataParam("remove") String membersToRemoveJson) {
        if (username == null || (spaceJson == null && membersToRemoveJson == null)) {
            throw new BadRequestException();
        }

        Space space = getSpace(spaceId);

        //modify space by changing name or adding a list of members
        if (spaceJson != null) {
            if (!(authorizer.canWrite(username, space.getPrivileges()))) {
                throw new ForbiddenException("You are not allowed to modify the space " + spaceId);
            }
            if (!JsonUtils.isJSONValid(spaceJson)) {
                logger.error("Posted json is not a valid json.");
                throw new BadRequestException("Posted json is not a valid json.");
            }

            String metadata = JsonUtils.extractValueFromJsonString(SPACE_METADATA, spaceJson);
            List<String> members = JsonUtils.extractValueListFromJsonString(SPACE_MEMBERS, spaceJson);
            if (metadata.equals("") && members.size() == 0) {
                throw new BadRequestException("Invalid identifiers");
            }
            //TODO: this will need to change once we add more fields to metadata
            if (!metadata.equals("")) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    SpaceMetadata newMetadata = objectMapper.readValue(metadata, SpaceMetadata.class);
                    String name = newMetadata.getName();
                    if (name.equals("")) {
                        throw new BadRequestException("Invalid name in metadata");
                    }
                    if (spaceRepository.getSpaceByName(name) == null) {
                        space.setMetadata(newMetadata);
                    } else {
                        throw new BadRequestException("New name of space already exists");
                    }
                } catch (IOException e) {
                    throw new BadRequestException("Invalid metadata. " + e.toString());
                }
            }
            if (members.size() > 0) {
                for (String id : members) {
                    addMembers(space, username, id);
                }
            }

            space = spaceRepository.addSpace(space);

            return space;
        }

        if (!(authorizer.canDelete(username, space.getPrivileges()))) {
            throw new ForbiddenException("You are not allowed to remove members from the space " + spaceId);
        }
        // modify space by deleting a list of members
        Members membersToDelete;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            membersToDelete = objectMapper.readValue(membersToRemoveJson, Members.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid members JSON. " + e.toString());
        }
        boolean spaceContainsMembers = false;
        List<String> members = membersToDelete.getMembers();
        for (String member: members) {
            if (space.hasMember(member)) {
                spaceContainsMembers = true;
                break;
            }
        }
        if (membersToDelete.getMembers().size() != 0 && spaceContainsMembers) {
            return removeMembers(username, space, membersToDelete);
        } else {
            throw new NotFoundException("The space does not contains the members defined to be removed.");
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/members/{memberId}")
    @ApiOperation(value = "Adds a member to a space")
    public Space addMembersToSpace(@HeaderParam("X-Credential-Username") String username,
                                   @ApiParam(value = "Space Id", required = true) @PathParam("id") String spaceId,
                                   @ApiParam(value = "Member Id", required = true) @PathParam("memberId") String memberId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        Space space = getSpace(spaceId);

        if (!authorizer.canWrite(username, space.getPrivileges())) {
            throw new NotAuthorizedException(username + " can't modify the space");
        }

        if (addMembers(space, username, memberId)) {
            return space;
        } else {
            throw new NotFoundException("Could not retrieve member with id " + memberId);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/grant")
    @ApiOperation(value = "Grants new privileges to a space")
    public Space grantPrivilegesToSpace(@HeaderParam("X-Credential-Username") String username,
                                        @ApiParam(value = "Space Id", required = true) @PathParam("id") String spaceId,
                                        @ApiParam(value = "JSON representing a privilege block", required = true) @FormDataParam("grant") String privilegesJson) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid json and username");
        }

        Space space = getSpace(spaceId);

        if (!authorizer.canWrite(username, space.getPrivileges())) {
            throw new NotAuthorizedException(username + " has not write permissions in " + spaceId);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Privileges privileges = objectMapper.readValue(privilegesJson, Privileges.class);
            space.addPrivileges(privileges);
        } catch (IOException e) {
            throw new BadRequestException("Invalid privileges JSON. " + e.toString());
        }

        spaceRepository.addSpace(space);

        return space;

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/members/{memberId}")
    @ApiOperation("Removes a member from a space")
    public Space removeMemberFromSpace(@HeaderParam("X-Credential-Username") String username,
                                       @ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId,
                                       @ApiParam(value = "Member id", required = true) @PathParam("memberId") String memberId) {
        if (username == null) {
            throw new BadRequestException("User must provide a valid username");
        }

        if (memberId == null) {
            throw new BadRequestException("User must provide a member Id or a list of member ids");
        }

        Space space = getSpace(spaceId);
        if (!authorizer.canDelete(username, space.getPrivileges())) {
            throw new NotAuthorizedException("User has no privileges to modify the space " + space.getName());
        }

        if (!space.hasMember(memberId)) {
            throw new NotFoundException("The member id was not found.");
        }

        Members membersToDelete = new Members();
        membersToDelete.addMember(memberId);

        return removeMembers(username, space, membersToDelete);

    }

    /**
     * If the space is not found it will throw a 404 error
     *
     * @param spaceId id of space
     * @return SpaceOld if one was found
     */
    private Space getSpace(String spaceId) {
        Space space = spaceRepository.getSpaceById(spaceId);
        if (space == null) {
            throw new NotFoundException("Could not find space with id " + spaceId);
        }
        return space;
    }

    /**
     * Makes an HTTP request to hazard service
     *
     * @param hazardId id of hazard
     * @param username username
     * @return A list of Json responses
     */
    private List<String> getHazards(String hazardId, String username) {
        //TODO: check if there is a better way of doing this
        HttpURLConnection con;
        try {
            List<URL> urls = new ArrayList<>();
            urls.add(new URL(SERVICES_URL + EARTHQUAKE_URL + hazardId));
            urls.add(new URL(SERVICES_URL + TORNADO_URL + hazardId));
            urls.add(new URL(SERVICES_URL + HURRICANE_URL + hazardId));
            urls.add(new URL(SERVICES_URL + TSUNAMI_URL + hazardId));
            try {
                for (URL url : urls) {
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("X-Credential-Username", username);
                    String content = getContent(con);
                    con.disconnect();
                    if (content != null) {
                        List<String> hazardDatasets = JsonUtils.extractValueListFromJsonString("hazardDatasets", content);
                        List<String> datasets = new ArrayList<>();
                        for (String hazardDataset : hazardDatasets) {
                            String id = JsonUtils.extractValueFromJsonString("datasetId", hazardDataset);
                            if (!id.equalsIgnoreCase("")) {
                                datasets.add(id);
                            }
                        }
                        //scenario tornado, tornado model, and scenario earthquake have different fields for dataset ids, we need to check all
                        if (hazardDatasets.size() == 0) {
                            String tornadoDataset = JsonUtils.extractValueFromJsonString("tornadoDatasetId", content);
                            if (!tornadoDataset.equalsIgnoreCase("")) {
                                datasets.add(tornadoDataset);
                            } else {
                                //check for raster dataset id in scenario earthquake
                                String rasterDataset = JsonUtils.extractValueFromJsonString("rasterDatasetId", content);
                                if (!rasterDataset.equalsIgnoreCase("")) {
                                    datasets.add(rasterDataset);
                                } else {
                                    String datasetId = JsonUtils.extractValueFromJsonString("datasetId", content);
                                    if (!datasetId.equalsIgnoreCase("")) {
                                        datasets.add(datasetId);
                                    }
                                }
                            }
                        }
                        //earthquake models have a rasterDataset sub document we need to check and try to add
                        if (hazardDatasets.size() == 0) {
                            String rasterDataset = JsonUtils.extractValueFromJsonString("rasterDataset", content);
                            if (!rasterDataset.equalsIgnoreCase("")) {
                                String rasterDatasetId = JsonUtils.extractValueFromJsonString("datasetId", rasterDataset);
                                if (!rasterDatasetId.equalsIgnoreCase("")) {
                                    datasets.add(rasterDatasetId);
                                }
                            }
                        }
                        //add the id of the hazard to the list so we can also add it to the space
                        datasets.add(hazardId);
                        return datasets;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Makes an HTTP request to a service
     *
     * @param serviceUrl String of service URL
     * @param memberId   Id of member
     * @param username   username
     * @return Json response of API call
     */
    private String get(String serviceUrl, String memberId, String username) {
        HttpURLConnection con;
        try {
            URL url = new URL(SERVICES_URL + serviceUrl + memberId);
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Credential-Username", username);
                return getContent(con);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the content of a HTTP request
     *
     * @param con the result of a http connection request
     * @return content of HTTP request
     */
    private String getContent(HttpURLConnection con) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            StringBuilder content = new StringBuilder();
            String jsonString;
            while ((jsonString = in.readLine()) != null) {
                content.append(jsonString);
            }
            in.close();
            return content.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Add member(s) to a space
     *
     * @param space    Space to modify
     * @param username username
     * @param memberId Id of member
     * @return True if a member was found in any service and was added to the space successfully. False if no member was
     * found or if the user has no write/admin privileges on a space that contains the member.
     */
    private boolean addMembers(Space space, String username, String memberId) {
        if (!authorizer.canUserWriteMember(username, memberId, spaceRepository.getAllSpaces())) {
            return false;
        }
        //TODO: SpaceController doesn't have to care about what is adding, so we need to rethink the design to avoid
        // the following conditional branching.
        //get dataset from data-service
        if (get(DATA_URL, memberId, username) != null) {
            space.addMember(memberId);
            spaceRepository.addSpace(space);
            return true;
        }

        //get fragility from fragility-service
        if (get(FRAGILITY_URL, memberId, username) != null) {
            space.addMember(memberId);
            spaceRepository.addSpace(space);
            return true;
        }

        //get a list containing the hazard and its associated datasets from hazard-service
        List<String> hazardDatasets = getHazards(memberId, username);
        //If the hazard has no datasets, we will just add the hazard id to the space
        if (hazardDatasets != null && hazardDatasets.size() > 0) {
            for (String id : hazardDatasets) {
                space.addMember(id);
            }
            spaceRepository.addSpace(space);
            return true;
        }
        return false;
    }

    /**
     * Remove members from a space. Keep track of members that are removed and no longer belong to any space.
     *
     * @param username        string username -- used for the moment to get all datasets associated to hazards
     * @param space           space to remove members from
     * @param membersToDelete list of members to remove
     * @return space with removed members if successful, unmodified space otherwise
     */
    private Space removeMembers(String username, Space space, Members membersToDelete) {
        //TODO: this will be removed in the future since spaces should not care about what they are removing
        List<String> deleteMembers = new ArrayList<>(membersToDelete.getMembers());
        for (String member : deleteMembers) {
            List<String> additionalMembers = getHazards(member, username);
            if (additionalMembers != null) {
                for (String newMember : additionalMembers) {
                    membersToDelete.addMember(newMember);
                }
            }
        }
        List<Space> spaces = spaceRepository.getAllSpaces();
        for (String member : membersToDelete.getMembers()) {
            space.removeMember(member);
        }

        spaceRepository.addSpace(space);

        //we need to keep track if a member is no longer a part of any space after removing
        Space orphansSpace = spaceRepository.getSpaceByName("orphans");

        //add all members that do not belong to any space to the orphans space (to be handled later)
        for (String removedMember : membersToDelete.getMembers()) {
            boolean isOrphan = true;
            for (Space searchSpace : spaces) {
                if (searchSpace.hasMember(removedMember)) {
                    isOrphan = false;
                    break;
                }
            }
            if (isOrphan) {
                orphansSpace.addMember(removedMember);
            }
        }
        spaceRepository.addSpace(orphansSpace);

        return space;

    }

}
