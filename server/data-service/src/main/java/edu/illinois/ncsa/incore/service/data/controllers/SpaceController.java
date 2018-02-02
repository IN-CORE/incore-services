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

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.HttpDownloader;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.geoserver.GeoserverUtils;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.MvzLoader;
import edu.illinois.ncsa.incore.service.data.models.Space;
import edu.illinois.ncsa.incore.service.data.models.impl.FileStorageDisk;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import edu.illinois.ncsa.incore.service.data.utils.FileUtils;
import edu.illinois.ncsa.incore.service.data.utils.JsonUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywkim on 7/26/2017.
 */

@Path("spaces")
public class SpaceController {
    private Logger logger = Logger.getLogger(SpaceController.class);

    @Inject
    private IRepository repository;

    //http://localhost:8080/data/api/spaces
    /**
     * return list of spaces in the database
     * @return list of spaces
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Space> getSpaceList() {
        List<Space> spaces = repository.getAllSpaces();
        if (spaces == null) {
            throw new NotFoundException("There is no Space in the repository.");
        }
        return spaces;
    }

}
