package edu.illinois.ncsa.incore.service.project.controllers;
/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.service.project.models.*;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
import edu.illinois.ncsa.incore.service.project.utils.ConversionUtils;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;


@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Project Service for creating and accessing projects",
        version = "1.26.1",
        title = "IN-CORE v2 Project Service API",
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
// Not sure if @Tag is equivalent to @Apis
@Tag(name = "projects")

@Path("projects")
public class ProjectController {
    private static final String DATA_SERVICE_URL = System.getenv("DATA_SERVICE_URL");
    private static final String HAZARD_SERVICE_URL = System.getenv("HAZARD_SERVICE_URL");
    private static final String DFR3_SERVICE_URL = System.getenv("DFR3_SERVICE_URL");
    private static final String EARTHQUAKE_URL = HAZARD_SERVICE_URL + "/hazard/api/earthquakes/";
    private static final String TORNADO_URL = HAZARD_SERVICE_URL + "/hazard/api/tornadoes/";
    private static final String HURRICANE_WF_URL = HAZARD_SERVICE_URL + "/hazard/api/hurricaneWindfields/";
    private static final String HURRICANE_URL = HAZARD_SERVICE_URL + "/hazard/api/hurricanes/";
    private static final String FLOOD_URL = HAZARD_SERVICE_URL + "/hazard/api/floods/";
    private static final String TSUNAMI_URL = HAZARD_SERVICE_URL + "/hazard/api/tsunamis/";
    private static final String FRAGILITY_URL = DFR3_SERVICE_URL + "/dfr3/api/fragilities/";
    private static final String MAPPING_URL = DFR3_SERVICE_URL + "/dfr3/api/mappings/";
    private static final String DATA_URL = DATA_SERVICE_URL + "/data/api/datasets/";

    private final Logger logger = Logger.getLogger(ProjectController.class);

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
    public ProjectController(
        @Parameter(name = "User credentials.", required = true) @HeaderParam("x-auth-userinfo") String userInfo,
        @Parameter(name = "User groups.", required = false) @HeaderParam("x-auth-usergroup") String userGroups
    ) {
        this.userGroups = userGroups;
        this.username = UserInfoUtils.getUsername(userInfo);
        this.groups = UserGroupUtils.getUserGroups(userGroups);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets the list of all available projects", description = "For member parameter, it will return projects that the" +
        " " +
        "user has read privileges on.")
    public List<Project> getProjectsList(@Parameter(name = "Name filter") @QueryParam("name") String name,
                                         @Parameter(name = "Creator filter") @QueryParam("creator") String creator,
                                         @Parameter(name = "Owner filter") @QueryParam("owner") String owner,
                                         @Parameter(name = "Region filter") @QueryParam("region") String region,
                                         @Parameter(name = "Type filter") @QueryParam("type") String type,
                                         @Parameter(name = "Name of space") @DefaultValue("") @QueryParam("space") String spaceName,
                                         @Parameter(name = "Skip the first n results") @QueryParam("skip") int offset,
                                         @Parameter(name = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        if (name != null) {
            queryMap.put("name", name);
        }
        if (creator != null) {
            queryMap.put("creator", creator);
        }
        if (owner != null) {
            queryMap.put("owner", owner);
        }
        if (region != null) {
            queryMap.put("region", region);
        }
        if (type != null) {
            queryMap.put("type", type);
        }

        List<Project> projects;

        if (queryMap.isEmpty()) {
            projects = this.projectDAO.getAllProjects();
        } else {
            projects = this.projectDAO.queryAllProjects(queryMap);
        }

        if (!spaceName.equals("")) {
            Space space = spaceRepository.getSpaceByName(spaceName);
            if (space == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a space with name " + spaceName);
            }
            if (!authorizer.canRead(username, space.getPrivileges(), groups)) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, username + " is not authorized to read the space " + spaceName);
            }
            List<String> spaceMembers = space.getMembers();

            projects = projects.stream()
                .filter(project -> spaceMembers.contains(project.getId()))
                .skip(offset)
                .limit(limit)
                .map(d -> {
                    d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                    return d;
                })
                .collect(Collectors.toList());

            return projects;
        }

        Set<String> membersSet = authorizer.getAllMembersUserHasReadAccessTo(username, spaceRepository.getAllSpaces(), groups);

        List<Project> accessibleProjects = projects.stream()
            .filter(b -> membersSet.contains(b.getId()))
            .skip(offset)
            .limit(limit)
            .map(d -> {
                d.setSpaces(spaceRepository.getSpaceNamesOfMember(d.getId()));
                return d;
            })
            .collect(Collectors.toList());

        return accessibleProjects;
    }

    @GET
    @Path("{projectId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gets a project by Id", summary = "Get a particular project based on the id provided")
    public Project getProjectById
        (@Parameter(name = "project id") @PathParam("projectId") String id) {
        Project project = this.projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                project.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return project;
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(description = "Ingest project object as json")
    public Project ingestProject(
        @Parameter(name = "JSON representing an input project", required = true) Project project) {

        UserInfoUtils.throwExceptionIfIdPresent(project.getId());

        project.setCreator(username);
        project.setOwner(username);

        Project savedProject = this.projectDAO.addProject(project);

        if (savedProject != null) {
            Space space = spaceRepository.getSpaceByName(username);
            if (space == null) {
                space = new Space(username);
                space.setPrivileges(Privileges.newWithSingleOwner(username));
            }
            space.addMember(savedProject.getId());
            spaceRepository.addSpace(space);
            project.setSpaces(spaceRepository.getSpaceNamesOfMember(project.getId()));

            return project;
        } else {
            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save project: returned null.");
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{projectId}")
    @Operation(description = "Update project")
    public Project updateProjectById(
        @Parameter(name = "project id") @PathParam("projectId") String id,
        @Parameter(name = "JSON representing an input project", required = true) Project newProject) {

        Project project = this.projectDAO.getProjectById(id);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }

        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (this.username.equals(project.getOwner()) || isAdmin) {
            Project updatedProject = this.projectDAO.updateProject(id, newProject);
            if (updatedProject != null) {
                // assume if can write, can read
                updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                return updatedProject;
            }
        }
        else{
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + "is not allowed to modify the project ");
        }

        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to update project.");
    }

    @PATCH
    @Path("{projectId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Patch project")
    public Project patchProjectById(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @FormParam("name") String name,
        @FormParam("description") String description,
        @FormParam("owner") String owner,
        @FormParam("region") String region,
        @FormParam("hazards") List<String> hazardListString,
        @FormParam("dfr3Mappings") List<String> dfr3MappingListString,
        @FormParam("datasets") List<String> datasetListString,
        @FormParam("workflows") List<String> workflowListString
    ) {
        Project project = this.projectDAO.getProjectById(id);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }

        // Authorization check
        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + "is not allowed to modify the project ");
        }

        // create new project
        if (name != null) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }
        if (owner != null) {
            project.setOwner(owner);
        }
        if (region != null) {
            project.setRegion(region);
        }
        if (hazardListString != null && hazardListString.size() > 0) {
            List<HazardResource> hazardResources = ConversionUtils.convertToHazardResources(hazardListString);
            project.setHazards(hazardResources);
        }
        if (dfr3MappingListString != null && dfr3MappingListString.size() > 0) {
            List<DFR3MappingResource> dfr3MappingResources = ConversionUtils.convertToDFR3MappingResources(dfr3MappingListString);
            project.setDfr3Mappings(dfr3MappingResources);
        }
        if (datasetListString != null && datasetListString.size() > 0) {
            List<DatasetResource> datasetResources = ConversionUtils.convertToDatasetResources(datasetListString);
            project.setDatasets(datasetResources);
        }
        if (workflowListString != null && workflowListString.size() > 0) {
            // Convert list of IDs to WorkflowResource objects and set them
            List<WorkflowResource> workflowResources = ConversionUtils.convertToWorkflowResources(workflowListString);
            project.setWorkflows(workflowResources);
        }

        // Update the project
        Project updatedProject = this.projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            return updatedProject;
        }

        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to patch the project.");
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{projectId}")
    @Operation(summary = "Deletes a project by id")
    public Project deleteProjectById(@Parameter(name = "project id") @PathParam("projectId") String id) {
        Project project = this.projectDAO.getProjectById(id);

        if (project != null) {
            boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (this.username.equals(project.getOwner()) || isAdmin) {
                // remove from space
                List<Space> spaces = spaceRepository.getAllSpaces();
                for (Space space : spaces) {
                    if (space.hasMember(id)) {
                        space.removeMember(id);
                        spaceRepository.addSpace(space);
                    }
                }
                return this.projectDAO.deleteProject(id);
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to delete the " +
                    "project with id " + id);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }
    }

    @POST
    @Path("{projectId}/datasets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add datasets to a project")
    public Project addDatasetsToProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "datasets", description = "List of datasets to add", required = true) List<DatasetResource> datasets) {

        // Validate the input
        if (datasets == null || datasets.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No datasets provided");
        }

        Project project = projectDAO.getProjectById(id);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }

        // Authorization check
        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + "is not allowed to modify the project ");
        }

        // Loop through datasets and add each one to the project
        for (DatasetResource dataset : datasets) {
            project.addDatasetResource(dataset);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add datasets to the project.");
    }

    @DELETE
    @Path("{projectId}/datasets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add datasets to a project")
    public Project deleteDatasetsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "datasets", description = "List of datasets to add", required = true) List<DatasetResource> datasets) {

        // Validate the input
        if (datasets == null || datasets.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No datasets provided");
        }

        Project project = projectDAO.getProjectById(id);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }

        // Authorization check
        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + "is not allowed to modify the project ");
        }

        // Loop through datasets and add each one to the project
        for (DatasetResource dataset : datasets) {
            project.deleteDatasetResource(dataset);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add datasets to the project.");
    }

}
