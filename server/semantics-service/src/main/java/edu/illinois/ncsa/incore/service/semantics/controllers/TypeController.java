package edu.illinois.ncsa.incore.service.semantics.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.semantics.daos.ITypeDAO;
import edu.illinois.ncsa.incore.service.semantics.model.Column;
import edu.illinois.ncsa.incore.service.semantics.model.Type;
import edu.illinois.ncsa.incore.service.semantics.utils.FileUtils;
import edu.illinois.ncsa.incore.service.semantics.utils.GeotoolsUtils;
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
import org.geotools.feature.DefaultFeatureCollection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.geotools.api.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static edu.illinois.ncsa.incore.service.semantics.utils.CommonUtil.typeComparator;

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Semantics Services for type and data type",
        version = "1.30.0",
        title = "IN-CORE v2 Semantics Service API",
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

@Tag(name = "types")

@Path("")
public class TypeController {
    private static final Logger log = Logger.getLogger(TypeController.class);

    private final String username;

    private final List<String> groups;

    private Configuration templateConfig;

    public static final String DATA_TEMP_DIR_PREFIX = "data_repo_";

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

        // Configure template and load available templates
        templateConfig = new Configuration();
        ClassTemplateLoader cloader = new ClassTemplateLoader(this.getClass(), "/templates/freemarker");
        templateConfig.setTemplateLoader(cloader);
    }

    @GET
    @Path("types")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "list all types belong user has access to.")
    public Response listTypes(
        @Parameter(name = "Name of the space.") @DefaultValue("") @QueryParam("space") String spaceName,
        @Parameter(name = "Specify the field or attribute on which the sorting is to be performed.") @DefaultValue("name") @QueryParam("sortBy") String sortBy,
        @Parameter(name = "Specify the order of sorting, either ascending or descending.") @DefaultValue("asc") @QueryParam("order") String order,
        @Parameter(name = "Skip the first n results.") @DefaultValue("0") @QueryParam("skip") int offset,
        @Parameter(name = "Limit number of results to return.") @DefaultValue("50") @QueryParam("limit") int limit,
        @Parameter(name = "List the hyperlinks.") @DefaultValue("false") @QueryParam("hyperlink") boolean hyperlink,
        @Parameter(name = "Return the full response.") @DefaultValue("false") @QueryParam("detail") boolean detail) {

        // import type comparator
        Comparator<Type> comparator = typeComparator(sortBy, order);

        List<Type> typeList = this.typeDAO.getTypes();

        // Filter out the types that belong to a given space if specified
        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            typeList = typeList.stream()
                .filter(type -> spaceMembers.contains(type.getId()))
                .collect(Collectors.toList());
        }

        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);
        List<Type> accessibleTypeList = typeList.stream()
            .filter(type -> userMembersSet.contains(type.getId()))
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        if (detail) {
            return Response.ok(accessibleTypeList.stream()
                    .map(type -> type.constructOutput())
                    .collect(Collectors.toList())
                )
                .status(200)
                .build();
        }

        List<String> results = accessibleTypeList.stream()
            .map(t -> t.getTitle())
            .collect(Collectors.toList());

        if (hyperlink) {
            results = results.stream().map(typename -> "/semantics/api/types/" + typename).collect(Collectors.toList());
        }

        return Response.ok(results).status(200).build();
    }

    public Type getTypesByName(String name, String version) {
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);
        List<Type> typeList = this.typeDAO.getTypeByName(name, version);

        if (typeList != null) {
            // make sure that uri is in the namespace
            List<Type> results = typeList.stream()
                .filter(type -> userMembersSet.contains(type.getId()))
                .collect(Collectors.toList());

            Type matchedType;

            // find the latest
            if (version.equals("latest")) {
                Optional<Type> latestMatched = results.stream()
                    .max(Comparator.comparing(Dtype ->
                        Double.parseDouble(Dtype.getVersion())));
                matchedType = latestMatched.orElse(null);
            } else {
                matchedType = results.get(0);
            }

            return matchedType;
        }

        return null;
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

        Type matchedType = getTypesByName(name, version);

        if (matchedType != null) {
            return Response.ok(matchedType.constructOutput()).status(200).build();
        }

        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Cannot find the type " + name + " version " + version + " !");
    }

    @GET
    @Path("types/{name}")
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Show specific types by uri as HTML.")
    public Response getTypeAsHtml(
        @Parameter(name = "Type uri (name).", required = true) @PathParam("name") String name,
        @Parameter(name = "version number.") @QueryParam("version") String version) throws JsonProcessingException {

        if (version == null) {
            version = "latest";
        }
        Type matchedType = getTypesByName(name, version);

        if (matchedType != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the Type to JSONObject
            String jsonString = objectMapper.writeValueAsString(matchedType.constructOutput());
            JSONObject typeJson = new JSONObject(jsonString);
            JSONObject tableSchema = typeJson.getJSONObject("tableSchema");
            JSONArray columnsArray = tableSchema.getJSONArray("columns");

            // Loop through each column
            List<Column> columns = new ArrayList<Column>();
            for (int i = 0; i < columnsArray.length(); i++) {
                JSONObject column = columnsArray.getJSONObject(i);
                String columnName = column.getString("name");
                String titles = column.getString("titles");
                String description = column.getString("dc:description");
                String datatype = column.getString("datatype");
                boolean required = Boolean.parseBoolean(column.getString("required"));
                String unit = column.getString("qudt:unit");
                columns.add(new Column(columnName, titles, datatype, description, unit, Boolean.toString(required)));
            }

            // Map of things to parse in the template - this can be expanded to add more objects
            // For example, we could pull the tableSchema into a separate Map so it can be parsed separately by the template
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("title", matchedType.getTitle());
            model.put("url", matchedType.getUrl());
            model.put("description", matchedType.getDescription());
            model.put("columns", columns);

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
        }

        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Cannot find the type " + name + " version " + version + " !");
    }

    @GET
    @Path("types/{name}/template")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Returns a template zip or a single file of the given dataset type.")
    public Response getTemplateByType(@Parameter(name = "Type uri (name).", required = true) @PathParam("name") String name,
                                      @Parameter(name = "version number.") @QueryParam("version") String version) throws IOException {
        if (version == null) {
            version = "latest";
        }
        Type matchedType = getTypesByName(name, version);

        if (matchedType != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the Type to JSONObject
            String jsonString = objectMapper.writeValueAsString(matchedType);
            JSONObject typeJson = new JSONObject(jsonString);
            JSONObject tableSchema = typeJson.getJSONObject("tableSchema");
            JSONArray columnsArray = tableSchema.getJSONArray("columns");

            // Collect column headers
            String[] headers = new String[columnsArray.length()];
            // Collect datatypes of columns
            List<String> dTypes = new ArrayList<>();
            // Shapefile flag to determine if the datatype should generate shapefile or csv based on Geometry column name's presence
            boolean shapefile = false;

            for (int i = 0; i < columnsArray.length(); i++) {
                JSONObject column = columnsArray.getJSONObject(i);
                String columnName = column.getString("name");
                String datatype = column.getString("datatype");
                headers[i] = columnName;
                dTypes.add(datatype);
                if (columnName.equalsIgnoreCase("geometry") || columnName.equalsIgnoreCase("the_geom") || columnName.equalsIgnoreCase("geom")) {
                    shapefile = true;
                }
            }

            // create temp dir and create files in temp dir
            String tempDir = Files.createTempDirectory(DATA_TEMP_DIR_PREFIX).toString();
            String fileName = name.replace(":", "_") + "_template";
            String ext = null;
            File outFile = null;

            if (shapefile) {
                ext = ".shp";
                System.out.println(tempDir + File.separator + fileName + ext);
                outFile = new File(tempDir + File.separator + fileName + ext);
                SimpleFeatureType schema = GeotoolsUtils.buildSchema(headers, dTypes);
                DefaultFeatureCollection collection = new DefaultFeatureCollection();
                outFile = GeotoolsUtils.outToFile(outFile, schema, collection);
                ext = ".zip"; // change the extension to zip
            } else {
                ext = ".csv";
                System.out.println(tempDir + File.separator + fileName + ext);
                outFile = new File(tempDir + File.separator + fileName + ext);
                FileUtils.writeHeadersToCsvFile(outFile, headers);
            }

            return Response.ok(outFile, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename=\"" + fileName + ext + "\"").build();

        }

        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Cannot find the type " + name + " version " + version + " !");
    }

    @GET
    @Path("types/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search type by partial match of text.")
    public Response searchType(
        @Parameter(name = "Type uri (name).") @QueryParam("text") String text,
        @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
        @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        Set<String> userMembersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<Type> typeList = this.typeDAO.searchType(text);
        List<Type> results;

        if (!typeList.isEmpty()) {
            results = typeList.stream()
                .filter(t -> userMembersSet.contains(t.getId()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
        } else {
            results = new ArrayList<>();
        }

        return Response.ok(results.stream()
                .map(type -> type.constructOutput())
                .collect(Collectors.toList())).status(200)
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
                Document newtype = this.typeDAO.postType(type);
                // add id to matching space
                String id = newtype.getObjectId("_id").toString();
                space.addMember(id);
                spaceRepository.addSpace(space);

                return Response.ok(newtype).status(200)
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

        if (!this.typeDAO.hasType(name))
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find type with name " + name);

        try {
            Type deletedType = this.typeDAO.deleteType(name);
            String deletedId = deletedType.getId();
            // remove id from spaces
            List<Space> spaces = spaceRepository.getAllSpaces();
            for (Space space : spaces) {
                if (space.hasMember(deletedId)) {
                    space.removeMember(deletedId);
                    spaceRepository.addSpace(space);
                }
            }
            return Response.ok(deletedType).status(200)
                .build();
        } catch (IncoreHTTPException e){
            throw e;
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid type JSON. " + e);
        }
    }
}
