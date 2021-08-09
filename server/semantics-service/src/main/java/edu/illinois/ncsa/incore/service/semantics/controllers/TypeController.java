package edu.illinois.ncsa.incore.service.semantics.controllers;

import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.semantics.daos.ITypeDAO;
import io.swagger.annotations.*;
import org.bson.Document;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

// @SwaggerDefinition is common for all the service's controllers and can be put in any one of them
@SwaggerDefinition(
    info = @Info(
        description = "IN-CORE Semantics Services for type and data type",
        version = "v0.6.3",
        title = "IN-CORE v2 Semantics Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://incore.ncsa.illinois.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    ),
    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
)

@Api(value = "types", authorizations = {})

@Path("")
public class TypeController {

    private String username;

    private Authorizer authorizer;

    @Inject
    private ITypeDAO typeDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    public TypeController(
        @ApiParam(value = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo) {
        this.username = UserInfoUtils.getUsername(userInfo);
        // we want to limit the semantics service to admins for now
        this.authorizer = new Authorizer();
        if (!this.authorizer.isUserAdmin(this.username)) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not an admin.");
        }
    }

    @GET
    @Path("types")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "list all types belong user has access to.")
    public Response listTypes() {
        List<Document> typeList = this.typeDAO.getTypes();
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());
        //return the intersection between all types and the ones the user can read
        List<Document> results = typeList.stream()
            .filter(type -> userMembersSet.contains(type.getObjectId("_id").toString()))
            .collect(Collectors.toList());

        return Response.ok(results).status(200)
            .build();

    }

    @GET
    @Path("types/{uri}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Show specific types by uri.")
    public Response getType(
        @ApiParam(value = "Type uri (name).", required = true) @PathParam("uri") String uri,
        @ApiParam(value = "version number.") @QueryParam("version") String version) {
        if (version == null) {
            version = "latest";
        }
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());
        Optional<List<Document>> typeList = this.typeDAO.getTypeByUri(uri, version);

        if (typeList.isPresent()) {
            // make sure that uri is in the namespace
            List<Document> results = typeList.get().stream()
                .filter(type -> userMembersSet.contains(type.getObjectId("_id").toString()))
                .collect(Collectors.toList());
            List<Document> matchedTypeList;

            // find the latest
            if (version.equals("latest")) {
                Optional<Document> latestMatched = results.stream()
                    .max(Comparator.comparing(Dtype -> Double.parseDouble(Dtype.get("openvocab:versionnumber").toString())));
                if (latestMatched.isPresent()) {
                    matchedTypeList = new ArrayList<Document>() {{
                        add(latestMatched.get());
                    }};
                } else {
                    matchedTypeList = new ArrayList<>();
                }
            } else {
                matchedTypeList = results;
            }

            return Response.ok(matchedTypeList).status(200)
                .build();

        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Cannot find the type " + uri + " version " + version + " !");
        }
    }

    @GET
    @Path("types/search")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search type by partial match of text.")
    public Response searchType(
        @ApiParam(value = "Type uri (name).") @QueryParam("text") String text) {
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces());

        Optional<List<Document>> typeList = this.typeDAO.searchType(text);
        List<Document> results;
        if (typeList.isPresent()) {
            results = typeList.get().stream()
                .filter(t -> userMembersSet.contains(t.getObjectId("_id").toString()))
                .collect(Collectors.toList());
        } else {
            results = new ArrayList<>();
        }

        return Response.ok(results).status(200)
            .build();
    }

    @POST
    @Path("/types")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Publish new type.")
    public Response publishType(
        @ApiParam(value = "Type uri (name).") Document type) {
        Space space = spaceRepository.getSpaceByName(this.username);

        String id = this.typeDAO.postType(type);

        // add id to matching space
        space.addMember(id);
        spaceRepository.addSpace(space);

        return Response.ok(id).status(200)
            .build();

    }

    @DELETE
    @Path("types/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete type by id.")
    public Response deleteType(
        @ApiParam(value = "Type id.") @PathParam("id") String id) {
        String deletedId = this.typeDAO.deleteType(id);
        if (deletedId == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find type with id " + id);
        }

        // TODO: when this service is not restricted to admins anymore, we will have to check if the user has permissions to delete
        // remove id from spaces
        List<Space> spaces = spaceRepository.getAllSpaces();
        for (Space space : spaces) {
            if (space.hasMember(deletedId)) {
                space.removeMember(deletedId);
                spaceRepository.addSpace(space);
            }
        }
        return Response.ok(deletedId).status(200)
            .build();

    }

}
