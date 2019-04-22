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

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

//TODO: Not enabling swagger docs because this controller is out of date with using X-Credential-Username
//@Api(value="mvzdatasets", authorizations = {})

@Path("mvzdatasets")
public class MvzDatasetController {
    private Logger logger = Logger.getLogger(MvzDatasetController.class);

    @Inject
    private IRepository repository;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of all MVZ Datasets", notes = "")
    public List<MvzDataset> getMvzDatasetList() {
        List<MvzDataset> mvzDatasets = repository.getAllMvzDatasets();
        if (mvzDatasets == null) {
            throw new NotFoundException("There is no SpaceOld in the repository.");
        }
        return mvzDatasets;
    }

}
