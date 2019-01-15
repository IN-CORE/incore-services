/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
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
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value="tsunamis", authorizations = {})

@Path("tsunamis")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error.")
})
public class TsunamiController {
    private static final Logger log = Logger.getLogger(TsunamiController.class);

    @Inject
    private ITsunamiRepository repository;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get all tsunamis.")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Internal Server Error.")
    })
    public List<Tsunami> getTsunamis(
        @HeaderParam("X-Credential-Username") String username) {

        return repository.getTsunamis().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }

    @GET
    @Path("{tsunami-id}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get all information about a tsunami.")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Internal Server Error."),
        @ApiResponse(code = 404, message = "Not Found - Invalid tsunami ID.")
    })
    public Tsunami getTsunami(
        @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tsunami dataset guid from data service.", required = true) @PathParam("tsunami-id") String tsunamiId) {

        Tsunami tsunami = repository.getTsunamiById(tsunamiId);
        if (tsunami == null) {
            throw new NotFoundException();
        }
        if (!authorizer.canRead(username, tsunami.getPrivileges())) {
            throw new ForbiddenException();
        }
        return tsunami;
    }

    @GET
    @Path("{tsunami-id}/value")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gets hazard value.")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Internal Server Error."),
        @ApiResponse(code = 404, message = "Not Found - Invalid tsunami ID."),
        @ApiResponse(code = 406, message = "Unsupported Format - Possibly the point parameter.")
    })
    public List<TsunamiHazardResult> getTsunamiHazardValues(
        @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Tsunami dataset guid from data service.", required = true) @PathParam("tsunami-id") String tsunamiId,
        @ApiParam(value = "Tsunami demand type. Ex: 'X'.", required = true) @QueryParam("demandType") String demandType,
        @ApiParam(value = "Tsunami demand unit. Ex: 'X'.", required = true) @QueryParam("demandUnits") String demandUnits,
        @ApiParam(value = "List of points provided as lat,long. Ex: '28.01,-83.85'.", required = true) @QueryParam("point") List<IncorePoint> points) {

        Tsunami tsunami = getTsunami(username, tsunamiId);
        List<TsunamiHazardResult> tsunamiResults = new LinkedList<>();
        if (tsunami != null) {
            for (IncorePoint point : points) {
                try {
                    tsunamiResults.add(TsunamiCalc.getTsunamiHazardValue(tsunami, demandType, demandUnits, point, username));
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
    @ApiOperation(value = "Create and post a tsunami.")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "Internal Server Error.")
    })
    public Tsunami createTsunami(
        @HeaderParam("X-Credential-Username") String username,
        @FormDataParam("tsunami") String tsunamiJson,
        @FormDataParam("file") List<FormDataBodyPart> fileParts) {

        ObjectMapper mapper = new ObjectMapper();
        Tsunami tsunami = null;
        try {
            tsunami = mapper.readValue(tsunamiJson, Tsunami.class);
            tsunami.setPrivileges(Privileges.newWithSingleOwner(username));

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

                        String datasetId = ServiceUtil.createRasterDataset(filename, bodyPartEntity.getInputStream(), tsunamiDataset.getName() + " " + datasetName, username, description, datasetType);
                        hazardDataset.setDatasetId(datasetId);
                    }
                    tsunami = repository.addTsunami(tsunami);
                    return tsunami;
                } else {
                    throw new BadRequestException("Could not create Tsunami, no files were attached with your request.");
                }
            }

        } catch (IOException e) {
            log.error("Error mapping the request to a supported Tsunami type.", e);
        }
        throw new BadRequestException("Could not create Tsunami, check the format of your request.");
    }
}
