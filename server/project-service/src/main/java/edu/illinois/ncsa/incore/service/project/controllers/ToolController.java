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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Project Service for creating and accessing tools",
        version = "1.29.0",
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
    @Path("/bldg-inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add NSI Building Inventory to existing project")
    public Project addBldInventoryToProject(
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

        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not authorized to modify this project.");
        }

        logger.debug("Making internal call to /data/api/datasets/tools/bldg-inventory");

        try {
            // Resolve dataset service endpoint
            String defaultDataEndpoint = "http://localhost:8080/";
            String dataEndpoint = Optional.ofNullable(System.getenv("DATA_SERVICE_URL"))
                .filter(s -> !s.isEmpty())
                .map(url -> url.endsWith("/") ? url : url + "/")
                .orElse(defaultDataEndpoint);
            String requestUrl = dataEndpoint + "data/api/datasets/tools/bldg-inventory";

            // Build HTTP POST with Authorization
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.setHeader("x-auth-userinfo", "{\"preferred_username\": \"" + this.username + "\"}");
            httpPost.setHeader("x-auth-usergroup", this.userGroups );

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(request);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("dataset", json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entityBuilder.build());

            // Execute the request
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpResponse response = httpclient.execute(httpPost);
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                logger.error("Dataset creation failed. Status: " + statusCode + ", Body: " + responseStr);
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Dataset creation failed: " + responseStr);
            }

            logger.debug("Dataset created successfully. Response: " + responseStr);

            // Parse dataset JSON string into <Map<String, Object>>
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> datasetMap = objectMapper.readValue(responseStr, new TypeReference<Map<String, Object>>() {
            });

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
                    fd.size = ((Number) fdMap.get("size")).longValue();
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

            project.addDatasetResource(dataset);
            projectDAO.updateProject(projectId, project);

            return project;

        } catch (JsonProcessingException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to serialize dataset request to JSON " + e.getMessage());
        } catch (IOException e) {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Internal request to dataset service failed " + e.getMessage());

        }
    }
}
