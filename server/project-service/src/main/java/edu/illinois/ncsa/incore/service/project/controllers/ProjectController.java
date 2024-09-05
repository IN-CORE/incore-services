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
import edu.illinois.ncsa.incore.service.project.utils.ServiceUtil;
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

import java.util.*;
import java.util.stream.Collectors;


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

                // TODO: Process all resources in the project
                // return ServiceUtil.processProjectResources(project, username, userGroups);
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
            // TODO
            // return ServiceUtil.processProjectResources(project, username, userGroups);

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

                // TODO
                // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
                return updatedProject;
            }
        } else {
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

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
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

    @GET
    @Path("{projectId}/datasets")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "List datasets to a project")
    public List<DatasetResource> listDatasetsOfProject(
        @Parameter(name = "projectId", description = "ID of the project.") @PathParam("projectId") String id) {
        Project project = projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                return project.getDatasets();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
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
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add datasets to the project.");
    }

    @DELETE
    @Path("{projectId}/datasets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete datasets to a project")
    public Project deleteDatasetsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "datasets", description = "List of datasets to delete", required = true) List<DatasetResource> datasets) {

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

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete datasets from the project.");
    }

    @GET
    @Path("{projectId}/dfr3mappings")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "List dfr3mappings to a project")
    public List<DFR3MappingResource> listDfr3MappingsOfProject(
        @Parameter(name = "projectId", description = "ID of the project.") @PathParam("projectId") String id) {
        Project project = projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                return project.getDfr3Mappings();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
    }

    @POST
    @Path("{projectId}/dfr3mappings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add dfr3mappings to a project")
    public Project addDfr3mappingsToProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "dfr3mappings", description = "List of dfr3mappings to add", required = true) List<DFR3MappingResource> dfr3mappings) {

        // Validate the input
        if (dfr3mappings == null || dfr3mappings.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No dfr3mappings provided");
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

        // Loop through dfr3mappings and add each one to the project
        for (DFR3MappingResource dfr3mapping : dfr3mappings) {
            project.addDFR3MappingResource(dfr3mapping);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add dfr3mappings to the project.");
    }

    @DELETE
    @Path("{projectId}/dfr3mappings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete dfr3mappings to a project")
    public Project deleteDfr3mappingsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "dfr3mappings", description = "List of dfr3mappings to delete", required = true) List<DFR3MappingResource> dfr3mappings) {

        // Validate the input
        if (dfr3mappings == null || dfr3mappings.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No dfr3mappings provided");
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

        // Loop through dfr3mappings and delete each one to the project
        for (DFR3MappingResource dfr3mapping : dfr3mappings) {
            project.deleteDFR3MappingResource(dfr3mapping);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete dfr3mappings from the project.");
    }

    @GET
    @Path("{projectId}/hazards")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "List hazards to a project")
    public List<HazardResource> listHazardsOfProject(
        @Parameter(name = "projectId", description = "ID of the project.") @PathParam("projectId") String id) {
        Project project = projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                return project.getHazards();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
    }

    @POST
    @Path("{projectId}/hazards")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add hazards to a project")
    public Project addHazardsToProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "hazards", description = "List of hazards to add", required = true) List<HazardResource> hazards) {

        // Validate the input
        if (hazards == null || hazards.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No hazards provided");
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

        // Loop through hazards and add each one to the project
        for (HazardResource hazard : hazards) {
            project.addHazardResource(hazard);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add hazards to the project.");
    }

    @DELETE
    @Path("{projectId}/hazards")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete hazards to a project")
    public Project deleteHazardsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "hazards", description = "List of hazards to delete", required = true) List<HazardResource> hazards) {

        // Validate the input
        if (hazards == null || hazards.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No hazards provided");
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

        // Loop through hazards and delete each one to the project
        for (HazardResource hazard : hazards) {
            project.deleteHazardResource(hazard);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete hazards from the project.");
    }

    @GET
    @Path("{projectId}/workflows")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "List workflows to a project")
    public List<WorkflowResource> listWorkflowsOfProject(
        @Parameter(name = "projectId", description = "ID of the project.") @PathParam("projectId") String id) {
        Project project = projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                return project.getWorkflows();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
    }

    @POST
    @Path("{projectId}/workflows")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add workflows to a project")
    public Project addWorkflowsToProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "workflows", description = "List of workflows to add", required = true) List<WorkflowResource> workflows) {

        // Validate the input
        if (workflows == null || workflows.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No workflows provided");
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

        // Loop through workflows and add each one to the project
        for (WorkflowResource workflow : workflows) {
            project.addWorkflowResource(workflow);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add workflows to the project.");
    }

    @DELETE
    @Path("{projectId}/workflows")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete workflows from a project")
    public Project deleteWorkflowsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "workflows", description = "List of workflows to delete", required = true) List<WorkflowResource> workflows) {

        // Validate the input
        if (workflows == null || workflows.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No workflows provided");
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

        // Loop through workflows and delete each one to the project
        for (WorkflowResource workflow : workflows) {
            project.deleteWorkflowResource(workflow);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete workflows from the project.");
    }

    @GET
    @Path("{projectId}/visualizations")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "List visualizations to a project")
    public List<VisualizationResource> listVisualizationsOfProject(
        @Parameter(name = "projectId", description = "ID of the project.") @PathParam("projectId") String id) {
        Project project = projectDAO.getProjectById(id);
        if (project != null) {
            if (authorizer.canUserReadMember(username, id, spaceRepository.getAllSpaces(), groups)) {
                return project.getVisualizations();
            } else {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " does not have privileges to access the " +
                    "project with id " + id);
            }
        }
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
    }

    @POST
    @Path("{projectId}/visualizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add visualizations to a project")
    public Project addVisualizationsToProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "visualizations", description = "List of visualizations to add", required = true) List<VisualizationResource> visualizations) {

        // Validate the input
        if (visualizations == null || visualizations.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No visualizations provided");
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

        // Loop through visualizations and add each one to the project
        for (VisualizationResource visualization : visualizations) {
            project.addVisualizationResource(visualization);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add visualizations to the project.");
    }

    @DELETE
    @Path("{projectId}/visualizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete visualizations from a project")
    public Project deleteVisualizationsFromeProject(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "visualizations", description = "List of visualizations to delete", required = true) List<VisualizationResource> visualizations) {

        // Validate the input
        if (visualizations == null || visualizations.isEmpty()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "No visualizations provided");
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

        // Loop through visualizations and delete each one to the project
        for (VisualizationResource visualization : visualizations) {
            project.deleteVisualizationResource(visualization);
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            // assume if can write, can read
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));

            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }
        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete visualizations from the project.");
    }

    @POST
    @Path("{projectId}/visualizations/{visualizationId}/layers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add new layer to visualization")
    public Project addLayersToMapVis(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "visualizationId", description = "ID of the visualization to update") @PathParam("visualizationId") String visualizationId,
        @Parameter(name = "layers", description = "List of layers to add", required = true) List<Layer> layers) {

        // Check if the project exists
        Project project = projectDAO.getProjectById(id);
        if (project == null) {
            throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
        }

        // Authorization check
        boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
        if (!this.username.equals(project.getOwner()) && !isAdmin) {
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not allowed to modify the project.");
        }

        // Check if the visualization is of type Map and update layers
        VisualizationResource visualization = project.getVisualization(visualizationId);
        if (visualization.getType().equals(VisualizationResource.Type.MAP)) {
            for (Layer layer : layers) {
                visualization.addLayer(layer);
            }
        } else {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Visualization with id " + visualizationId + " is not a Map.");
        }

        // Update the project in the database
        Project updatedProject = projectDAO.updateProject(id, project);
        if (updatedProject != null) {
            updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
            // TODO
            // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
            return updatedProject;
        }

        throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to add layer.");
    }

    @DELETE
    @Path("{projectId}/visualizations/{visualizationId}/layers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete layers from a project")
    public Project deleteLayersFromMapVis(
        @Parameter(name = "projectId", description = "ID of the project to update") @PathParam("projectId") String id,
        @Parameter(name = "visualizationId", description = "ID of the visualization to update", required = true) @PathParam("visualizationId") String visualizationId,
        @Parameter(name = "layers", description = "List of layers to delete", required = true) List<Layer> layers) {
        {
            // Check if the project exists
            Project project = projectDAO.getProjectById(id);
            if (project == null) {
                throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project with id " + id);
            }

            // Authorization check
            boolean isAdmin = Authorizer.getInstance().isUserAdmin(this.groups);
            if (!this.username.equals(project.getOwner()) && !isAdmin) {
                throw new IncoreHTTPException(Response.Status.FORBIDDEN, this.username + " is not allowed to modify the project.");
            }

            // Check if the visualization is of type Map and update layers
            VisualizationResource visualization = project.getVisualization(visualizationId);
            if (visualization.getType().equals(VisualizationResource.Type.MAP)) {
                for (Layer layer : layers) {
                    visualization.removeLayer(layer);
                }
            } else {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Visualization with id " + visualizationId + " is not a Map.");
            }

            // Update the project in the database
            Project updatedProject = projectDAO.updateProject(id, project);
            if (updatedProject != null) {
                updatedProject.setSpaces(spaceRepository.getSpaceNamesOfMember(id));
                // TODO
                // return ServiceUtil.processProjectResources(updatedProject, username, userGroups);
                return updatedProject;
            }

            throw new IncoreHTTPException(Response.Status.INTERNAL_SERVER_ERROR, "Failed to delete the layer.");
        }
    }
}
