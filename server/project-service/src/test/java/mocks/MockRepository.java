package mocks; /*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
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
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.models.Project;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements ISpaceRepository {
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
    public List<Project> getAllSpaces() {
        return this.projects;
    }

    @Override
    public Project getSpaceById(String id) {
        for (int i = 0; i < this.projects.size(); i++) {
            if (this.projects.get(i).getId().equalsIgnoreCase(id)) {
                return this.projects.get(i);
            }
        }
        return null;
    }

    @Override
    public Project getSpaceByName(String name) {
        for (int i = 0; i < this.projects.size(); i++) {
            if (this.projects.get(i).getName().equalsIgnoreCase(name)) {
                return this.projects.get(i);
            }
        }
        return null;
    }

    @Override
    public Project addSpace(Project project) {
        this.projects.add(project);
        return this.projects.get(this.projects.size() - 1);
    }

    @Override
    public Project getOrphanSpace() {
        return getSpaceByName("orphans");
    }

    @Override
    public Project addToOrphansSpace(String memberId) {
        return getSpaceByName("orphans");
    }

    @Override
    public Project deleteSpace(String id) {
        return null;
    }

    @Override
    public List<String> getSpaceNamesOfMember(String memberId) {
        return null;
    }
}
