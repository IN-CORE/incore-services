package edu.illinois.ncsa.incore.service.semantics.controllers;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.semantics.daos.ITypeDAO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@OpenAPIDefinition(
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
    )
)

@Tag(name = "types")

@Path("")
public class TypeController {

    private final String username;

    private final List<String> groups;

    @Inject
    private ITypeDAO typeDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    private IAuthorizer authorizer;

    @Inject
    public TypeController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Path("types")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "list all types belong user has access to.")
    public Response listTypes(
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("asc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results.") @DefaultValue("0") @QueryParam("skip") int offset,
        @Parameter(name = "Limit number of results to return.") @DefaultValue("50") @QueryParam("limit") int limit,
        @Parameter(name = "List the hyperlinks.") @DefaultValue("false") @QueryParam("hyperlink") boolean hyperlink) {
        Comparator<String> comparator = Comparator.naturalOrder();
        if (order.equals("desc")) comparator = comparator.reversed();

        List<Document> typeList = this.typeDAO.getTypes();
        List<String> results = typeList.stream()
            .map(t -> t.get("dc:title").toString())
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        if (hyperlink) {
            String typeEndpoint = "http://localhost:8080/";
            String typeEndpointProp = System.getenv("SERVICES_URL");
            if (typeEndpointProp != null && !typeEndpointProp.isEmpty()) {
                typeEndpoint = typeEndpointProp;
                if (!typeEndpoint.endsWith("/")) {
                    typeEndpoint += "/";
                }
            }
            String finalTypeEndpoint = typeEndpoint;
            results = results.stream().map(typename -> finalTypeEndpoint + typename).collect(Collectors.toList());
        }

        return Response.ok(results).status(200)
            .build();
    }

    @GET
    @Path("types/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Show specific types by uri.")
    public Response getType(
        @Parameter(name = "Type uri (name).", required = true) @PathParam("name") String name,
        @Parameter(name = "version number.") @QueryParam("version") String version) {
        if (version == null) {
            version = "latest";
        }
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);
        Optional<List<Document>> typeList = this.typeDAO.getTypeByName(name, version);

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
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Cannot find the type " + name + " version " + version + " !");
        }
    }

    @GET
    @Path("types/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search type by partial match of text.")
    public Response searchType(
        @Parameter(name = "Type uri (name).") @QueryParam("text") String text) {
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

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
    @Operation(summary = "Publish new type.")
    public Response publishType(
        @Parameter(name = "Type uri (name).") Document type) {
        try {
            if (authorizer.isUserAdmin(this.groups)) {
                Space space = spaceRepository.getSpaceByName(this.username);
                String id = this.typeDAO.postType(type);
                // add id to matching space
                space.addMember(id);
                spaceRepository.addSpace(space);

                return Response.ok(id).status(200)
                    .build();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not an admin.");
            }
        } catch (IncoreHTTPException e){
            throw e;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid type JSON. " + e);
        }
    }

    @DELETE
    @Path("types/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete type by name.")
    public Response deleteType(
        @Parameter(name = "Type name.") @PathParam("name") String name) {
        // TODO: when this service is not restricted to admins anymore, we will have to check if the user has permissions to delete
        if (!authorizer.isUserAdmin(this.groups))
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not an admin.");

        try{
            String deletedId = this.typeDAO.deleteType(name);
            if (deletedId.equals("")) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find type with name " + name);
            }
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
        } catch (IncoreHTTPException e){
            throw e;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid type JSON. " + e);
        }
    }
}
