/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 ********************************************************************************/

package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.util.List;

//TODO: Not enabling swagger docs because this controller is out of date with using X-Credential-Username
//@Api(value="files", authorizations = {})

@Path("files")
public class FileController {
    private final Logger logger = Logger.getLogger(edu.illinois.ncsa.incore.service.data.controllers.FileController.class);
    private static final String DATA_REPO_FOLDER = System.getenv("DATA_REPO_DATA_DIR");
    private final String username;
    private final List<String> groups;

    @Inject
    private IRepository repository;

    @Inject
    public FileController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    // TODO why are below endpoints not access controlled?

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of all files' metadata", description = "")
    public List<FileDescriptor> getFileDescriptorList() {
        List<FileDescriptor> fds = repository.getAllFileDescriptors();
        if (fds == null) {
            logger.error("There is no FileDescriptors in the repository.");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "There are no file descriptors in the repository.");
        }
        return fds;
    }


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets metadata of a file's metadata", description = "")
    public FileDescriptor getFileDescriptorById(@Parameter(name = "FileDescriptor Object Id", required = true) @PathParam("id") String id) {
        Dataset dataset = repository.getDatasetByFileDescriptorId(id);
        if (dataset == null) {
            logger.error("Error finding dataset.");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find the dataset " + id);
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String fdId = "";
        FileDescriptor fileDescriptor = null;

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(id)) {
                fileDescriptor = fd;
                break;
            }
        }

        if (fileDescriptor == null) {
            logger.error("Error finding FileDesriptor with given id.");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a file descriptor with id " + id);
        }

        return fileDescriptor;
    }

    /**
     * get file by using FileDescriptor ID
     *
     * @param id
     * @return
     */
    @GET
    @Path("{id}/blob")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Returns a file linked to the FileDescriptor Object", description = "")
    public Response getFileByFileDescriptorId(@Parameter(name = "FileDescriptor Object Id") @PathParam("id") String id) {
        File outFile = null;
        Dataset dataset = repository.getDatasetByFileDescriptorId(id);
        if (dataset == null) {
            logger.error("Error finding dataset.");
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a file descriptor with id " + id);
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = "";
        String fdId = "";
        String fileName = "";

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(id)) {
                dataUrl = FilenameUtils.concat(DATA_REPO_FOLDER, fd.getDataURL());
                fileName = fd.getFilename();
                break;
            }
        }

        if (!dataUrl.equals("")) {
            outFile = new File(dataUrl);
            outFile.renameTo(new File(outFile.getParentFile(), fileName));
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"").build();
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find file with id " + id);
        }
    }
}
