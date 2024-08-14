/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.project.dao;

import dev.morphia.Datastore;
import edu.illinois.ncsa.incore.service.project.models.Project;

import java.util.List;

public interface IRepository {
    void initialize();

    List<Project> getAllProjects();
    List<Project> getProjectsByType(String type);
    List<Project> getProjectsByName(String name);
    List<Project> getProjectsByCreator(String creator);
    List<Project> getProjectsByOwner(String owner);
    List<Project> getProjectsByRegion(String region);

    Project getProjectById(String id);

    List<Project> searchProjects(String text);

    // TODO: update project method
    Project updateProject(Project project);

    Project addProject(Project project);

    Project deleteProject(String id);

    Datastore getDataStore();

}
