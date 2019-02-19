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

@Path("tsunamis")
public class TsunamiController {
    private static final Logger log = Logger.getLogger(TsunamiController.class);

    @Inject
    private ITsunamiRepository repository;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Tsunami> getTsunamis(@HeaderParam("X-Credential-Username") String username) {
        return repository.getTsunamis().stream()
            .filter(d -> authorizer.canRead(username, d.getPrivileges()))
            .collect(Collectors.toList());
    }

    @GET
    @Path("{tsunami-id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Tsunami getTsunami(@HeaderParam("X-Credential-Username") String username, @PathParam("tsunami-id") String tsunamiId) {
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
    @Path("{tsunami-id}/values")
    @Produces({MediaType.APPLICATION_JSON})
    public List<TsunamiHazardResult> getTsunamiHazardValues(@HeaderParam("X-Credential-Username") String username, @PathParam("tsunami-id") String tsunamiId, @QueryParam("demandType") String demandType, @QueryParam("demandUnits") String demandUnits, @QueryParam("point") List<IncorePoint> points) {
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
    public Tsunami createTsunami(@HeaderParam("X-Credential-Username") String username, @FormDataParam("tsunami") String tsunamiJson, @FormDataParam("file") List<FormDataBodyPart> fileParts) {
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
