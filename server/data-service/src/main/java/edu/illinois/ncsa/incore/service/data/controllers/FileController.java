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
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

//TODO: Not enabling swagger docs because this controller is out of date with using X-Credential-Username
//@Api(value="files", authorizations = {})

@Path("files")
public class FileController {
    private final Logger logger = Logger.getLogger(edu.illinois.ncsa.incore.service.data.controllers.FileController.class);
    private static final String DATA_REPO_FOLDER = System.getenv("DATA_REPO_DATA_DIR");

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of all files' metadata", notes = "")
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
    @ApiOperation(value = "Gets metadata of a file's metadata", notes = "")
    public FileDescriptor getFileDescriptorById(@ApiParam(value = "FileDescriptor Object Id", required = true) @PathParam("id") String id) {
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
    @ApiOperation(value = "Returns a file linked to the FileDescriptor Object", notes = "")
    public Response getFileByFileDescriptorId(@ApiParam(value = "FileDescriptor Object Id") @PathParam("id") String id) {
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
