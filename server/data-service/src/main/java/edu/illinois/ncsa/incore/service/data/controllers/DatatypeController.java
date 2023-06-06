package edu.illinois.ncsa.incore.service.data.controllers;

import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.models.DatasetType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "datatypes", authorizations = {})

@Path("datatypes")
public class DatatypeController {
    private final Logger logger = Logger.getLogger(DatatypeController.class);
    private final String username;
    private final List<String> groups;

    @Inject
    private IRepository repository;

    @Inject
    public DatatypeController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @ApiParam(value = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
        ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the list of unique datatypes associated with the datasets", notes = "")
    public List<DatasetType> getDatatypes(@ApiParam(value = "Filter by space name", required = false) @QueryParam("space") String spaceName) {
        return repository.getDatatypes(spaceName);
    }

}
