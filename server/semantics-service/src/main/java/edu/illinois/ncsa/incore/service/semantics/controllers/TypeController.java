package edu.illinois.ncsa.incore.service.semantics.controllers;

import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.semantics.daos.ITypeDAO;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
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
    private static final Logger log = Logger.getLogger(TypeController.class);

    private final String username;

    private final Authorizer authorizer;
    private final List<String> groups;

    private Configuration templateConfig;

    @Inject
    private ITypeDAO typeDAO;

    @Inject
    private ISpaceRepository spaceRepository;

    @Inject
    public TypeController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
        // we want to limit the semantics service to admins for now
        this.authorizer = new Authorizer();
        if (!this.authorizer.isUserAdmin(this.groups)) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not an admin.");
        }

        // Configure template and load available templates
        templateConfig = new Configuration();
        ClassTemplateLoader cloader = new ClassTemplateLoader(this.getClass(), "/templates/freemarker");
        templateConfig.setTemplateLoader(cloader);
    }

    @GET
    @Path("types")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "list all types belong user has access to.")
    public Response listTypes() {
        List<Document> typeList = this.typeDAO.getTypes();
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);
        //return the intersection between all types and the ones the user can read
        List<Document> results = typeList.stream()
            .filter(type -> userMembersSet.contains(type.getObjectId("_id").toString()))
            .collect(Collectors.toList());

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
    @Path("types/{name}")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Show specific types by uri as HTML.")
    public Response getTypeAsHtml(
        @Parameter(name = "Type uri (name).", required = true) @PathParam("name") String name,
        @Parameter(name = "version number.") @QueryParam("version") String version) {

        // Since this code is shared with the endpoint that returns JSON, it should be pulled out into a utility to avoid duplication
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

            Document d = matchedTypeList.get(0);

            // Convert the BSON Document to a JSONObject
            JSONObject typeJson = new JSONObject(d.toJson());
            JSONObject tableSchema = typeJson.getJSONObject("tableSchema");
            JSONArray columnsArray = tableSchema.getJSONArray("columns");

//            // Loop through each column
//            for (int i = 0; i < columnsArray.length(); i++) {
//                JSONObject column = columnsArray.getJSONObject(i);
//
//                // Extract properties of the column
//                String name = column.getString("name");
//                String titles = column.getString("titles");
//                String description = column.getString("dc:description");
//                String datatype = column.getString("datatype");
//                boolean required = Boolean.parseBoolean(column.getString("required"));
//                String unit = column.getString("qudt:unit");
//            }
            // Map of things to parse in the template - this can be expanded to add more objects
            // For example, we could pull the tableSchema into a separate Map so it can be parsed separately by the template
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("title", d.get("dc:title"));
            model.put("description", d.get("dc:description"));
//            model.put("columns", columnsArray);

            try {
                Template typeTemplate = templateConfig.getTemplate("types.ftl");
                StringWriter output = new StringWriter();
                typeTemplate.process(model, output);
                return Response.ok(output.toString()).status(200).build();
            } catch (IOException e) {
                log.error("Could not read the template file to generate html page", e);
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Could not read the template file to generate html " +
                    "page");
            } catch (TemplateException e) {
                log.error("Could not process type object using the template file", e);
                e.printStackTrace();
                throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Could not process type object using the template " +
                    "file");
            }
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
    @Operation(summary = "Delete type by id.")
    public Response deleteType(
        @Parameter(name = "Type id.") @PathParam("id") String id) {
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
