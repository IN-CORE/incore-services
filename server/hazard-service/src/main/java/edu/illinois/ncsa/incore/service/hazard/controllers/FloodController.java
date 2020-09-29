/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.hazard.HazardConstants;
import edu.illinois.ncsa.incore.service.hazard.dao.IFloodRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.flood.*;
import edu.illinois.ncsa.incore.service.hazard.models.flood.types.FloodHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.flood.utils.FloodCalc;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "floods", authorizations = {})

@Path("floods")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class FloodController {
    private static final Logger log = Logger.getLogger(FloodController.class);
    private String username;

    @Inject
    private IFloodRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public FloodController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all floods.")
    public List<Flood> getFloods(
        @ApiParam(value = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Flood> floods = repository.getFloods();
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any space with name " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges())) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            floods = floods.stream()
                .filter(flood -> spaceMembers.contains(flood.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            return floods;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());
        List<Flood> accessibleFloods = floods.stream()
            .filter(flood -> membersSet.contains(flood.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleFloods;
    }

    @GET
    @Path("{flood-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the flood with matching id.")
    public Flood getFloodById(
        @ApiParam(value = "Flood dataset guid from data service.", required = true) @PathParam("flood-id") String floodId) {

        Flood flood = repository.getFloodById(floodId);
        if (flood == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a flood with id " + floodId);
        }

        if (authorizer.canUserReadMember(this.username, floodId, spaceRepository.getAllSpaces())) {
            return flood;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You are not authorized to access the flood " + floodId);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new flood, the newly created flood is returned.",
        notes="Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create dataset-based floods only.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "flood", value = "Flood json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Flood files.", required = true, dataType = "string", paramType = "form")
    })
    public Flood createFlood(
        @ApiParam(hidden = true) @FormDataParam("flood") String floodJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        ObjectMapper mapper = new ObjectMapper();
        Flood flood = null;
        try {
            flood = mapper.readValue(floodJson, Flood.class);

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (flood != null && flood instanceof FloodDataset) {
                FloodDataset floodDataset = (FloodDataset) flood;

                // We assume the input files in the request are in the same order listed in the flood dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty()) {
                    for (FormDataBodyPart filePart : fileParts) {
                        FloodHazardDataset hazardDataset = floodDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String datasetType = HazardConstants.DETERMINISTIC_FLOOD_HAZARD_SCHEMA;
                        String description = "Deterministic hazard raster";
//                        TODO if we have probabilistic flood in the future
//                        if (hazardDataset instanceof ProbabilisticFloodHazard) {
//                              //enable this when we get a probabilistic flood
//                            description = "Probabilistic hazard raster";
//                            datasetType = HazardConstants.PROBABILISTIC_FLOOD_HAZARD_SCHEMA;
//                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        String datasetName = demandType;
                        BodyPartEntity bodyPartEntity = (BodyPartEntity)filePart.getEntity();
                        String filename = filePart.getContentDisposition().getFileName();

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(),
                            floodDataset.getName() + " " + datasetName, this.username, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }

                    flood.setCreator(this.username);
                    flood = repository.addFlood(flood);

                    Space space = spaceRepository.getSpaceByName(this.username);
                    if(space == null) {
                        space = new Space(this.username);
                        space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    }
                    space.addMember(flood.getId());
                    spaceRepository.addSpace(space);

                    return flood;
                } else {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create flood, no files were attached with your request.");
                }
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported flood type.", e);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument has been passed in.", e);
        }

        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create flood, check the format of your request.");
    }

    @GET
    @Path("{flood-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the specified flood values.")
    public List<FloodHazardResult> getFloodHazardValues(
        @ApiParam(value = "Flood dataset guid from data service.", required = true) @PathParam("flood-id") String floodId,
        @ApiParam(value = "Flood demand type. Ex: 'inundationDepth, waterSurfaceElevation'.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Flood demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '46.01,-123.94'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Flood flood = getFloodById(floodId);
        List<FloodHazardResult> floodResults = new LinkedList<>();
        if (flood != null) {
            for (IncorePoint point : points) {
                try {
                    floodResults.add(FloodCalc.getFloodHazardValue(flood, demandType,
                        demandUnits, point, this.username));
                } catch (UnsupportedHazardException e) {
                    log.error("Could not get the requested hazard type. Check that the hazard type " + demandType + " and units " + demandUnits + " are supported", e);
                }
            }
        }

        return floodResults;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{flood-id}")
    @ApiOperation(value = "Deletes a flood", notes = "Also deletes attached datasets and related files")
    public Flood deleteFlood(@ApiParam(value = "Flood Id", required = true) @PathParam("flood-id") String floodId) {
        Flood flood = getFloodById(floodId);

        if (authorizer.canUserDeleteMember(this.username, floodId, spaceRepository.getAllSpaces())) {
            // delete associated datasets
            if (flood != null && flood instanceof FloodDataset) {
                FloodDataset hurrDataset = (FloodDataset) flood;
                for (FloodHazardDataset dataset : hurrDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }
//            else if(flood != null && flood instanceof FloodModel){
//                // add this when ready to migrate FloodWindfields
//            }

            Flood deletedHurr = repository.deleteFloodById(floodId); // remove flood json

            //remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(floodId)) {
                    space.removeMember(floodId);
                    spaceRepository.addSpace(space);
                }
            }

            return deletedHurr;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " flood " + floodId);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all floods", notes = "Gets all floods that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No floods found with the searched text")
    })
    public List<Flood> findFloods(
        @ApiParam(value = "Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Flood> floods;
        Flood flood = repository.getFloodById(text);
        if (flood != null) {
            floods = new ArrayList<Flood>() {{
                add(flood);
            }};
        } else {
            floods = this.repository.searchFloods(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        floods = floods.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return floods;
    }

}
