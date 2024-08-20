package edu.illinois.ncsa.incore.service.project.controllers;
/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************
 */

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.Space;
import edu.illinois.ncsa.incore.service.project.models.Project;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import edu.illinois.ncsa.incore.common.utils.UserGroupUtils;
import edu.illinois.ncsa.incore.common.utils.UserInfoUtils;
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

    private static final String SPACE_MEMBERS = "members";
    private static final String SPACE_METADATA = "metadata";

    private final Logger logger = Logger.getLogger(ProjectController.class);

    private final String username;
    private final List<String> groups;
    private final String userGroups;

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
    public Project getFragilityById
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
        throw new IncoreHTTPException(Response.Status.NOT_FOUND, "Could not find a project  with id " + id);
    }
}
