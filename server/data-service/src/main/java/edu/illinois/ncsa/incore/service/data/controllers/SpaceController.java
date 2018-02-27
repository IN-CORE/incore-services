/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Space;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("spaces")
public class SpaceController {
    private Logger logger = Logger.getLogger(SpaceController.class);

    @Inject
    private IRepository repository;

    /**
     * return list of spaces in the database
     * @return list of spaces
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Space> getSpaceList() {
        List<Space> spaces = repository.getAllSpaces();
        if (spaces == null) {
            logger.error("There is no Space in the repository.");
            throw new NotFoundException("There is no Space in the repository.");
        }
        return spaces;
    }

}
