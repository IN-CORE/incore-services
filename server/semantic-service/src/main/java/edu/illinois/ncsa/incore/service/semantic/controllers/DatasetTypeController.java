package edu.illinois.ncsa.incore.service.semantic.controllers;

import com.mongodb.util.JSON;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.semantic.daos.IDatasetTypeDAO;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.annotations.*;
import org.bson.Document;

// @SwaggerDefinition is common for all the service's controllers and can be put in any one of them
@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Semantic Services for dataset type and data type",
        version = "v0.3.0",
        title = "IN-CORE v2 Semantic Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore2.ncsa.illinois.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    ),
    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
//    ,tags = {
//        @Tag(name = "Private", description = "Tag used to denote operations as private")
//    },
    //externalDocs = @ExternalDocs(value = "FEMA  Hazard Manual", url = "https://www.fema.gov/earthquake")
)

@Api(value = "datasettypes", authorizations = {})

@Path("")
public class DatasetTypeController {

    private String username;

    @Inject
    private IDatasetTypeDAO datasetTypeDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public DatasetTypeController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
    }

    @GET
    @Path("/spaces/{namespace}/datasettypes")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "list all datasettypes belong to a namespace.")
    public Response listDatasetTypes(@ApiParam(value="Space name.", required=true)
                                         @PathParam("namespace") String namespace) {
        Space space = spaceRepository.getSpaceByName(namespace);
        if (space == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "No space was found with the name " + namespace);
        }
        // check if user has permission to read space
        if (!authorizer.canRead(username, space.getPrivileges())) {
            return Response.status(401).entity(username + " is not authorized to read the space " + namespace).build();
        }

        // find intersection
        List<Document> datasetTypeList = this.datasetTypeDAO.getDatasetTypes();
        List<Document> results = datasetTypeList.stream()
            .filter(datasetType -> space.hasMember(datasetType.getObjectId("_id").toString()))
            .collect(Collectors.toList());

        String serializedResults = JSON.serialize(results);

        if (results != null) {
            return Response.ok(serializedResults).status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
        } else {
            return Response.status(404).entity("No datasettypes in this space!").build();
        }
    }

    @GET
    @Path("/spaces/{namespace}/datasettypes/{uri}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Show specific datasettypes by uri.")
    public Response getDatasetType(
        @ApiParam(value = "Space name.", required = true) @PathParam("namespace") String namespace,
        @ApiParam(value = "datasettype uri (name).", required = true) @PathParam("uri") String uri,
        @ApiParam(value = "version number.") @QueryParam("version") String version) {
        if (version == null) {
            version = "latest";
        }

        Space space = spaceRepository.getSpaceByName(namespace);
        if (space != null){
            // check if user has permission to read space
            if (!authorizer.canRead(username, space.getPrivileges())) {
                return Response.status(401).entity(username + " is not authorized to read the space " + namespace).build();
            }

            Optional<List<Document>> datasetTypeList = this.datasetTypeDAO.getDatasetTypeByUri(uri, version);

            if (datasetTypeList.isPresent()) {
                // make sure that uri is in the namespace
                List<Document> results = datasetTypeList.get().stream()
                    .filter(dType -> space.hasMember(dType.getObjectId("_id").toString()))
                    .collect(Collectors.toList());

                List<Document> matchedDatasetTypeList;

                // find the latest
                if (version.equals("latest")) {
                    Optional<Document> latestMatched = results.stream()
                        .max(Comparator.comparing(Dtype -> Double.parseDouble(Dtype.get("openvocab:versionnumber").toString())));
                    if (latestMatched.isPresent()) { matchedDatasetTypeList = new ArrayList<Document>() {{ add(latestMatched.get()); }}; }
                    else { matchedDatasetTypeList = new ArrayList<>(); }
                }
                else{
                    matchedDatasetTypeList = results;
                }

                String serializedResults = JSON.serialize(matchedDatasetTypeList);

                return Response.ok(serializedResults).status(200)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET")
                    .build();

            } else {
                return Response.status(404).entity("Cannot find the datasettype " + uri + " version " + version + " !").build();
            }
        }
        else{
            return Response.status(404).entity("Cannot find the space " + namespace + "!").build();
        }
    }

    @GET
    @Path("/spaces/{namespace}/datasettypes/search")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Search dataset type by partial match of datasettype.")
    public Response searchDatasetType(
        @ApiParam(value = "Space name.", required = true) @PathParam("namespace") String namespace,
        @ApiParam(value = "Dataset type uri (name).") @QueryParam("datasettype") String datasettype) {

        if (datasettype == null) {
            throw new WebApplicationException(
                Response.status(400)
                    .entity("the parameter \"datasettype\" is required!").build()
            );
        }

        Space space = spaceRepository.getSpaceByName(namespace);
        if (space != null) {
            // check if user has permission to read space
            if (!authorizer.canRead(username, space.getPrivileges())) {
                return Response.status(401).entity(username + " is not authorized to read the space " + namespace).build();
            }

            Optional<List<Document>> datasetTypeList = this.datasetTypeDAO.searchDatasetType(datasettype);
            List<Document> results;
            if (datasetTypeList.isPresent()) {
                results = datasetTypeList.get().stream()
                    .filter(dType -> space.hasMember(dType.getObjectId("_id").toString()))
                    .collect(Collectors.toList());
            } else {
                results = new ArrayList<>();
            }

            String serializedResults = JSON.serialize(results);

            return Response.ok(serializedResults).status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
        }
        else{
            return Response.status(404).entity("Cannot find the space " + namespace + "!").build();
        }
    }

    @POST
    @Path("/spaces/{namespace}/datasettype")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value="Publish new datasetType.")
    public Response publishDatasetType(
        @ApiParam(value = "Space name.") @PathParam("namespace") String namespace,
        @ApiParam(value = "Dataset type uri (name).") Document datasetType) {

        Space space = spaceRepository.getSpaceByName(namespace);
        if (space != null) {
            // check if user has permission to write to space
            if (!authorizer.canWrite(username, space.getPrivileges())) {
                return Response.status(401).entity(username + " is not authorized to write to the space " + namespace).build();
            }

            String id = this.datasetTypeDAO.postDatasetType(datasetType);

            // add id to matching space
            space.addMember(id);
            spaceRepository.addSpace(space);

            return Response.ok(id).status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
        }
        else{
            return Response.status(404).entity("Cannot find the space " + namespace + "!").build();
        }
    }

    @DELETE
    @Path("/spaces/{namespace}/datasettypeId/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete datasetType by id.")
    public Response deleteDatasetType(
        @ApiParam(value = "User credentials.") @HeaderParam("X-Credential-Username") String username,
        @ApiParam(value = "Space name.") @PathParam("namespace") String namespace,
        @ApiParam(value = "Dataset type id.") @PathParam("id") String id) {

        Space space = spaceRepository.getSpaceByName(namespace);
        if (space != null) {
            // check if user has permission to write to space
            if (!authorizer.canWrite(username, space.getPrivileges())) {
                return Response.status(401).entity(username + " is not authorized to write to the space " + namespace).build();
            }

            String deletedId = this.datasetTypeDAO.deleteDatasetType(id);

            // remove id in the matching space
            space.removeMember(deletedId);
            spaceRepository.addSpace(space);

            return Response.ok(deletedId).status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
        }
        else{
            return Response.status(404).entity("Cannot find the space " + namespace + "!").build();
        }
    }

}
