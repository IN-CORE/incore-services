package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.DatasetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "datatypes")

@Path("datatypes")
public class DatatypeController {
    private final Logger logger = Logger.getLogger(DatatypeController.class);
    private final String username;
    private final List<String> groups;

    @Inject
    private IRepository repository;

    @Inject
    public DatatypeController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
        ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of unique datatypes associated with the datasets", description = "")
    public List<DatasetType> getDatatypes(@Parameter(name = "Filter by space name", required = false) @QueryParam("space") String spaceName) {
        return repository.getDatatypes(spaceName);
    }

}
