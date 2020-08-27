/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
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
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.*;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.types.HurricaneHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.utils.HurricaneCalc;
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

@Api(value = "hurricanes", authorizations = {})

@Path("hurricanes")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class HurricaneController {
    private static final Logger log = Logger.getLogger(HurricaneController.class);
    private String username;

    @Inject
    private IHurricaneRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public HurricaneController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all hurricanes.")
    public List<Hurricane> getHurricanes(
        @ApiParam(value = "Name of space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Hurricane> hurricanes = repository.getHurricanes();
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find any space with name " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges())) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            hurricanes = hurricanes.stream()
                .filter(hurricane -> spaceMembers.contains(hurricane.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            return hurricanes;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());
        List<Hurricane> accessibleHurricanes = hurricanes.stream()
            .filter(hurricane -> membersSet.contains(hurricane.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleHurricanes;
    }

    @GET
    @Path("{hurricane-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the hurricane with matching id.")
    public Hurricane getHurricaneById(
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricane-id") String hurricaneId) {

        Hurricane hurricane = repository.getHurricaneById(hurricaneId);
        if (hurricane == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a hurricane with id " + hurricaneId);
        }

        if (authorizer.canUserReadMember(this.username, hurricaneId, spaceRepository.getAllSpaces())) {
            return hurricane;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, "You are not authorized to access the hurricane " + hurricaneId);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new hurricane, the newly created hurricane is returned.",
        notes="Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create dataset-based hurricanes only.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "hurricane", value = "Hurricane json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Hurricane files.", required = true, dataType = "string", paramType = "form")
    })
    public Hurricane createHurricane(
        @ApiParam(hidden = true) @FormDataParam("hurricane") String hurricaneJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        ObjectMapper mapper = new ObjectMapper();
        Hurricane hurricane = null;
        try {
            hurricane = mapper.readValue(hurricaneJson, Hurricane.class);

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (hurricane != null && hurricane instanceof HurricaneDataset) {
                HurricaneDataset hurricaneDataset = (HurricaneDataset) hurricane;

                // We assume the input files in the request are in the same order listed in the hurricane dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty()) {
                    for (FormDataBodyPart filePart : fileParts) {
                        HurricaneHazardDataset hazardDataset = hurricaneDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String datasetType = HazardConstants.DETERMINISTIC_HURRICANE_HAZARD_SCHEMA;
                        String description = "Deterministic hazard raster";
//                        if (hazardDataset instanceof ProbabilisticHurricaneHazard) {
//                              //enable this when we get a probabilistic hurricane
//                            description = "Probabilistic hazard raster";
//                            datasetType = HazardConstants.PROBABILISTIC_HURRICANE_HAZARD_SCHEMA;
//                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        String datasetName = demandType;
                        BodyPartEntity bodyPartEntity = (BodyPartEntity)filePart.getEntity();
                        String filename = filePart.getContentDisposition().getFileName();

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(),
                            hurricaneDataset.getName() + " " + datasetName, this.username, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }

                    hurricane.setCreator(this.username);
                    hurricane = repository.addHurricane(hurricane);

                    Space space = spaceRepository.getSpaceByName(this.username);
                    if(space == null) {
                        space = new Space(this.username);
                        space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    }
                    space.addMember(hurricane.getId());
                    spaceRepository.addSpace(space);

                    return hurricane;
                } else {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create hurricane, no files were attached with your request.");
                }
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported hurricane type.", e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Could not map the request to a supported hurricane type. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument has been passed in.", e);
        }
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create hurricane, check the format of your request.");
    }

    @GET
    @Path("{hurricane-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the specified hurricane values.")
    public List<HurricaneHazardResult> getHurricaneHazardValues(
        @ApiParam(value = "Hurricane dataset guid from data service.", required = true) @PathParam("hurricane-id") String hurricaneId,
        @ApiParam(value = "Hurricane demand type. Ex: 'waveHeight, surgeLevel, inundationDuration'.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Hurricane demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '46.01,-123.94'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Hurricane hurricane = getHurricaneById(hurricaneId);
        List<HurricaneHazardResult> hurricaneResults = new LinkedList<>();
        if (hurricane != null) {
            for (IncorePoint point : points) {
                try {
                    hurricaneResults.add(HurricaneCalc.getHurricaneHazardValue(hurricane, demandType,
                        demandUnits, point, this.username));
                } catch (UnsupportedHazardException e) {
                    log.error("Could not get the requested hazard type. Check that the hazard type " + demandType + " and units " + demandUnits + " are supported", e);
                }
            }
        }

        return hurricaneResults;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hurricane-id}")
    @ApiOperation(value = "Deletes a Hurricane", notes = "Also deletes attached datasets and related files")
    public Hurricane deleteHurricaneWindfields(@ApiParam(value = "Hurricane Id", required = true) @PathParam("hurricane-id") String hurricaneId) {
        Hurricane hurricane = getHurricaneById(hurricaneId);

        if (authorizer.canUserDeleteMember(this.username, hurricaneId, spaceRepository.getAllSpaces())) {
            // delete associated datasets
            if (hurricane != null && hurricane instanceof HurricaneDataset) {
                HurricaneDataset hurrDataset = (HurricaneDataset) hurricane;
                for (HurricaneHazardDataset dataset : hurrDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }
//            else if(hurricane != null && hurricane instanceof HurricaneModel){
//                // add this when ready to migrate HurricaneWindfields
//            }

            Hurricane deletedHurr = repository.deleteHurricaneById(hurricaneId); // remove hurricane json

            //remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(hurricaneId)) {
                    space.removeMember(hurricaneId);
                    spaceRepository.addSpace(space);
                }
            }

            return deletedHurr;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " hurricane " + hurricaneId);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all hurricanes", notes = "Gets all hurricanes that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No hurricanes found with the searched text")
    })
    public List<Hurricane> findHurricanes(
        @ApiParam(value = "Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Hurricane> hurricanes;
        Hurricane hurricane = repository.getHurricaneById(text);
        if (hurricane != null) {
            hurricanes = new ArrayList<Hurricane>() {{
                add(hurricane);
            }};
        } else {
            hurricanes = this.repository.searchHurricanes(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        hurricanes = hurricanes.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return hurricanes;
    }

}
