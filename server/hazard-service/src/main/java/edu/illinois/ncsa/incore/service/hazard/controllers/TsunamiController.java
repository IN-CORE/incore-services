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
import edu.illinois.ncsa.incore.service.hazard.dao.ITsunamiRepository;
import edu.illinois.ncsa.incore.service.hazard.exception.UnsupportedHazardException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.DeterministicTsunamiHazard;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiDataset;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.TsunamiHazardDataset;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.types.TsunamiHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.utils.TsunamiCalc;
import edu.illinois.ncsa.incore.service.hazard.utils.ServiceUtil;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value="tsunamis", authorizations = {})

@Path("tsunamis")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error.")
})
public class TsunamiController {
    private static final Logger log = Logger.getLogger(TsunamiController.class);
    private String username;

    @Inject
    private ITsunamiRepository repository;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public TsunamiController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
            this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns all tsunamis.")
    public List<Tsunami> getTsunamis(
        @ApiParam(value = "Space name") @DefaultValue("") @QueryParam("space") String spaceName,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the space " + spaceName);
            }
            if (!authorizer.canRead(this.username, space.getPrivileges())) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();
            List<Tsunami> tsunamis = repository.getTsunamis();
            tsunamis = tsunamis.stream()
                .filter(tsunami -> spaceMembers.contains(tsunami.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
            return tsunamis;
        }
        List<Tsunami> tsunamis = repository.getTsunamis();

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        List<Tsunami> accessibleTsunamis = tsunamis.stream()
            .filter(tsunami -> membersSet.contains(tsunami.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return accessibleTsunamis;
    }

    @GET
    @Path("{tsunami-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the scenario tsunami matching the given id.")
    public Tsunami getTsunami(
        @ApiParam(value = "Tsunami dataset guid from data service.", required = true) @PathParam("tsunami-id") String tsunamiId) {

        Tsunami tsunami = repository.getTsunamiById(tsunamiId);
        if (tsunami == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a tsunami with id " + tsunamiId);
        }

        if (authorizer.canUserReadMember(this.username, tsunamiId, spaceRepository.getAllSpaces())) {
            return tsunami;
        }

        throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have the privileges to access " + tsunamiId);
    }

    @GET
    @Path("{tsunami-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns the specified tsunami values.")
    public List<TsunamiHazardResult> getTsunamiHazardValues(
        @ApiParam(value = "Tsunami dataset guid from data service.", required = true) @PathParam("tsunami-id") String tsunamiId,
        @ApiParam(value = "Tsunami demand type. Ex: 'Hmax, Vmax, Mmax'.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Tsunami demand unit. Ex: 'm'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '46.01,-123.94'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Tsunami tsunami = getTsunami(tsunamiId);
        List<TsunamiHazardResult> tsunamiResults = new LinkedList<>();
        if (tsunami != null) {
            for (IncorePoint point : points) {
                try {
                    tsunamiResults.add(TsunamiCalc.getTsunamiHazardValue(tsunami, demandType, demandUnits, point, this.username));
                } catch (UnsupportedHazardException e) {
                    log.error("Could not get the requested hazard type. Check that the hazard type " + demandType + " and units " + demandUnits + " are supported", e);
                }
            }
        }

        return tsunamiResults;
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new tsunami, the newly created tsunami is returned.",
        notes="Additionally, a GeoTiff (raster) is created by default and publish to data repository. " +
            "User can create dataset-based tsunamis only.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tsunami", value = "Tsunami json.", required = true, dataType = "string", paramType = "form"),
        @ApiImplicitParam(name = "file", value = "Tsunami files.", required = true, dataType = "string", paramType = "form")
    })
    public Tsunami createTsunami(
        @ApiParam(hidden = true) @FormDataParam("tsunami") String tsunamiJson,
        @ApiParam(hidden = true) @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        ObjectMapper mapper = new ObjectMapper();
        Tsunami tsunami = null;
        try {
            tsunami = mapper.readValue(tsunamiJson, Tsunami.class);

            // Create temporary working directory
            File incoreWorkDirectory = File.createTempFile("incore", ".dir");
            incoreWorkDirectory.delete();
            incoreWorkDirectory.mkdirs();

            if (tsunami != null && tsunami instanceof TsunamiDataset) {
                TsunamiDataset tsunamiDataset = (TsunamiDataset) tsunami;

                // We assume the input files in the request are in the same order listed in the tsunami dataset object
                int hazardDatasetIndex = 0;
                if (fileParts != null && !fileParts.isEmpty()) {
                    for (FormDataBodyPart filePart : fileParts) {
                        TsunamiHazardDataset hazardDataset = tsunamiDataset.getHazardDatasets().get(hazardDatasetIndex);
                        String datasetType = HazardConstants.PROBABILISTIC_TSUNAMI_HAZARD_SCHEMA;
                        String description = "Probabilistic hazard raster";
                        if (hazardDataset instanceof DeterministicTsunamiHazard) {
                            description = "Deterministic hazard raster";
                            datasetType = HazardConstants.DETERMINISTIC_TSUNAMI_HAZARD_SCHEMA;
                        }
                        hazardDatasetIndex++;

                        String demandType = hazardDataset.getDemandType();
                        String datasetName = demandType;
                        BodyPartEntity bodyPartEntity = (BodyPartEntity)filePart.getEntity();
                        String filename = filePart.getContentDisposition().getFileName();

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(), tsunamiDataset.getName() + " " + datasetName,
                            this.username, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }

                    tsunami.setCreator(this.username);
                    tsunami = repository.addTsunami(tsunami);

                    Space space = spaceRepository.getSpaceByName(this.username);
                    if(space == null) {
                        space = new Space(this.username);
                        space.setPrivileges(Privileges.newWithSingleOwner(this.username));
                    }
                    space.addMember(tsunami.getId());
                    spaceRepository.addSpace(space);

                    return tsunami;
                } else {
                    throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create Tsunami, no files were attached with your request.");
                }
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported Tsunami type.", e);
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Could not map the request to a supported Tsunami type. " + e.getMessage());
        }
        throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not create Tsunami, check the format of your request.");
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tsunami-id}")
    @ApiOperation(value = "Deletes a Tsunami", notes = "Also deletes attached dataset and related files")
    public Tsunami deleteTsunami(@ApiParam(value = "Tsunami Id", required = true) @PathParam("tsunami-id") String tsunamiId) {
        Tsunami tsunami = getTsunami(tsunamiId);

        if (authorizer.canUserDeleteMember(this.username, tsunamiId, spaceRepository.getAllSpaces())) {
            //remove associated datasets
            if (tsunami != null && tsunami instanceof TsunamiDataset) {
                TsunamiDataset tsuDataset = (TsunamiDataset) tsunami;
                for (TsunamiHazardDataset dataset : tsuDataset.getHazardDatasets()) {
                    if (ServiceUtil.deleteDataset(dataset.getDatasetId(), this.username) == null) {
                        spaceRepository.addToOrphansSpace(dataset.getDatasetId());
                    }
                }
            }

            Tsunami deletedTsunami = repository.deleteTsunamiById(tsunamiId); // remove tsunami json

            // remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(tsunamiId)) {
                    space.removeMember(tsunamiId);
                    spaceRepository.addSpace(space);
                }
            }

            return deletedTsunami;
        } else {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to delete the" +
                " tsunami " + tsunamiId);
        }
    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all tsunamis", notes="Gets all tsunamis that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No tsunamis found with the searched text")
    })
    public List<Tsunami> findTsunamis(
        @ApiParam(value="Text to search by", example = "building") @QueryParam("text") String text,
        @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
        @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        List<Tsunami> tsunamis;
        Tsunami tsunami = repository.getTsunamiById(text);
        if (tsunami != null) {
            tsunamis = new ArrayList<Tsunami>() {{
                add(tsunami);
            }};
        } else {
            tsunamis = this.repository.searchTsunamis(text);
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(this.username, spaceRepository.getAllSpaces());

        tsunamis = tsunamis.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return tsunamis;
    }

}
