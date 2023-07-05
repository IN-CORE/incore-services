/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

//TODO: Not enabling swagger docs because this controller is out of date with using X-Credential-Username
//@Api(value="mvzdatasets", authorizations = {})

@Path("mvzdatasets")
public class MvzDatasetController {
    private final Logger logger = Logger.getLogger(MvzDatasetController.class);

    @Inject
    private IRepository repository;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of all MVZ Datasets", description = "")
    public List<MvzDataset> getMvzDatasetList() {
        List<MvzDataset> mvzDatasets = repository.getAllMvzDatasets();
        if (mvzDatasets == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "There are no MVZ datasets in the repository.");
        }
        return mvzDatasets;
    }

}
