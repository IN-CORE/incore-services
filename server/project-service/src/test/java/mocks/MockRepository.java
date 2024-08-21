package mocks; /*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *   Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.service.project.models.Project;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MockRepository implements IProjectRepository {
    private final Datastore mockDataStore;
    private List<Project> projects = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL spacesPath = this.getClass().getClassLoader().getResource("json/projects.json");

        try {
            this.projects = new ObjectMapper().readValue(spacesPath, new TypeReference<List<Project>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.find(Project.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.projects);
    }

    @Override
    public List<Project> getAllProjects() {
        return null;
    }

    @Override
    public List<Project> queryAllProjects(Map<String, String> queryMap) {
        return null;
    }

    @Override
    public List<Project> getProjectsByType(String type) {
        return null;
    }

    @Override
    public List<Project> getProjectsByName(String name) {
        return null;
    }

    @Override
    public List<Project> getProjectsByCreator(String creator) {
        return null;
    }

    @Override
    public List<Project> getProjectsByOwner(String owner) {
        return null;
    }

    @Override
    public List<Project> getProjectsByRegion(String region) {
        return null;
    }

    @Override
    public Project getProjectById(String id) {
        return null;
    }

    @Override
    public List<Project> searchProjects(String text) {
        return null;
    }

    @Override
    public Project updateProject(String projectId, Project newProject) {
        return null;
    }

    @Override
    public Project addProject(Project project) {
        return null;
    }

    @Override
    public Project deleteProject(String id) {
        return null;
    }

    @Override
    public Datastore getDataStore() {
        return null;
    }
}
