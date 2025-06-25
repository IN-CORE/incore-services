package edu.illinois.ncsa.incore.service.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import edu.illinois.ncsa.incore.service.project.models.BldInventoryRequest;
import edu.illinois.ncsa.incore.service.project.models.Project;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.log4j.Logger;

import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Project Service for creating and accessing tools",
        version = "1.28.0",
        title = "IN-CORE v2 Project Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://tools.in-core.org"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    )
)
// Not sure if @Tag is equivalent to @Apis
@Tag(name = "tools")

@Path("tools")
public class ToolController {
    private final Logger logger = Logger.getLogger(ToolController.class);

    private final String username;
    private final List<String> groups;
    private final String userGroups;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private IProjectRepository projectDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public ToolController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @POST
    @Path("/bld-inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add NSI Building Inventory to existing project")
    public Response addBldInventoryToProject(
        @Parameter(description = "Project ID", required = true)
        @QueryParam("projectid") String projectId,
        @Parameter(description = "JSON payload with title, description, and FIPS list", required = true)
            BldInventoryRequest request
    ) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Missing required query parameter: projectid");
        }

        Project project = projectDAO.getProjectById(projectId);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Project not found with id: " + projectId);
        }
        // Authorization check
        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to modify this project.");
        }

        // TODO: perform the actual logic of processing FIPS and adding data

        return Response.ok().entity("{\"message\": \"NSI Building Inventory successfully added.\"}").build();
    }
}
