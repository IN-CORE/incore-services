package edu.illinois.ncsa.incore.service.project.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import edu.illinois.ncsa.incore.service.project.models.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Make internal call to /data/api/datasets/tools/bldg-inventory
        Client client = ClientBuilder.newBuilder()
            .register(MultiPartFeature.class)
            .build();
        try {
            FormDataMultiPart multipart = new FormDataMultiPart()
                .field("dataset", objectMapper.writeValueAsString(request), MediaType.APPLICATION_JSON_TYPE);
            Response response = client
                // TODO clean the hardcode later
                .target("http://localhost:8080/data/api/datasets/tools/bldg-inventory")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multipart, multipart.getMediaType()));

            if (response.getStatus() != 200) {
                throw new RuntimeException("Dataset creation failed: " + response.readEntity(String.class));
            }
            String responseJson = response.readEntity(String.class);

            // Parse dataset JSON string into <Map<String, Object>>
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> datasetMap = objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});

            // Construct DatasetResource list
            DatasetResource dataset = new DatasetResource();
            dataset.setId((String) datasetMap.get("id"));
            dataset.title = ((String) datasetMap.get("title"));
            dataset.description = ((String) datasetMap.get("description"));
            dataset.setType((String) datasetMap.get("dataType"));
            dataset.setDataType((String) datasetMap.get("dataType"));
            dataset.format = ((String) datasetMap.get("format"));
            dataset.creator = ((String) datasetMap.get("creator"));
            dataset.owner = ((String) datasetMap.get("owner"));
            // Contributors
            Object contributorsObj = datasetMap.get("contributors");
            if (contributorsObj instanceof List<?>) {
                List<?> rawList = (List<?>) contributorsObj;
                dataset.contributors = rawList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            }

            // FileDescriptors
            Object fileDescObj = datasetMap.get("fileDescriptors");
            if (fileDescObj instanceof List<?>) {
                List<Map<String, Object>> fileDescList = (List<Map<String, Object>>) fileDescObj;
                List<FileDescriptor> fileDescriptors = new ArrayList<>();
                for (Map<String, Object> fdMap : fileDescList) {
                    FileDescriptor fd = new FileDescriptor();
                    fd.id = ((String) fdMap.get("id"));
                    fd.filename = ((String) fdMap.get("filename"));
                    fd.deleted = ((Boolean) fdMap.get("deleted"));
                    fd.mimeType = ((String) fdMap.get("mimeType"));
                    fd.size = ((long) fdMap.get("size"));
                    fd.dataURL = ((String) fdMap.get("dataURL"));
                    fd.md5sum = ((String) fdMap.get("md5sum"));
                    fileDescriptors.add(fd);
                }
                dataset.fileDescriptors = fileDescriptors;
            }

            // BoundingBox
            Object bbox = datasetMap.get("boundingBox");
            if (bbox instanceof List<?>) {
                List<?> bboxList = (List<?>) bbox;
                double[] bbArray = bboxList.stream()
                    .filter(Number.class::isInstance)
                    .mapToDouble(v -> ((Number) v).doubleValue())
                    .toArray();
                dataset.boundingBox = bbArray;
            }

            // Add to project
            project.addDatasetResource(dataset);

            // Add to project
            project.addDatasetResource(dataset);

            // Update the project in the database
            projectDAO.updateProject(projectId, project);

            return Response.ok().entity("{\"message\": \"NSI Building Inventory successfully added.\"}").build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize dataset request to JSON", e);
        }


    }
}
