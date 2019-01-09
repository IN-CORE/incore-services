/*
 * ******************************************************************************
 *   Copyright (c) 2018 University of Illinois and others.  All rights reserved.
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
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

//TODO: Not enabling swagger docs because this controller is out of date with using X-Credential-Username
//@Api(value="files", authorizations = {})

@Path("files")
public class FileController {
    private Logger logger = Logger.getLogger(edu.illinois.ncsa.incore.service.data.controllers.FileController.class);

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of all files' metadata", notes = "")
    public List<FileDescriptor> getFileDescriptorList() {
        List<FileDescriptor> fds = repository.getAllFileDescriptors();
        if (fds == null) {
            logger.error("There is no FileDescriptors in the repository.");
            throw new NotFoundException("There is no FileDesctiptor in the repository.");
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
            throw new NotFoundException("Error finding dataset.");
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
            throw new NotFoundException("Error finding FileDesriptor with given id.");
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
    @ApiOperation(value = "Returns a file linked to the FileDescriptor Object", notes="")
    public Response getFileByFileDescriptorId(@ApiParam(value = "FileDescriptor Object Id") @PathParam("id") String id) {
        File outFile = null;
        Dataset dataset = repository.getDatasetByFileDescriptorId(id);
        if (dataset == null) {
            logger.error("Error finding dataset.");
            throw new NotFoundException("Error finding dataset.");
        }

        List<FileDescriptor> fds = dataset.getFileDescriptors();
        String dataUrl = "";
        String fdId = "";
        String fileName = "";

        for (FileDescriptor fd : fds) {
            fdId = fd.getId();
            if (fdId.equals(id)) {
                dataUrl = fd.getDataURL();
                fileName = fd.getFilename();
                break;
            }
        }

        if (!dataUrl.equals("")) {
            try {
                outFile = new File(new URI(dataUrl));
                outFile.renameTo(new File(outFile.getParentFile(), fileName));
            } catch (URISyntaxException e) {
                logger.error("Error making file using file id ", e);
                throw new InternalServerErrorException("Error making file using file id ", e);
            }
        }

        if (outFile != null) {
            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
        } else {
            throw new NotFoundException("Error finding file with the given id");
        }
    }
}
