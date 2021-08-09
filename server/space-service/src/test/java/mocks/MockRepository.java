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
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.models.Space;
import org.mockito.Mockito;
import dev.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements ISpaceRepository {
    private Datastore mockDataStore;
    private List<Space> spaces = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL spacesPath = this.getClass().getClassLoader().getResource("json/spaces.json");

        try {
            this.spaces = new ObjectMapper().readValue(spacesPath, new TypeReference<List<Space>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.find(Space.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.spaces);
    }


    @Override
    public List<Space> getAllSpaces() {
        return this.spaces;
    }

    @Override
    public Space getSpaceById(String id) {
        for (int i = 0; i < this.spaces.size(); i++) {
            if (this.spaces.get(i).getId().equalsIgnoreCase(id)) {
                return this.spaces.get(i);
            }
        }
        return null;
    }

    @Override
    public Space getSpaceByName(String name) {
        for (int i = 0; i < this.spaces.size(); i++) {
            if (this.spaces.get(i).getName().equalsIgnoreCase(name)) {
                return this.spaces.get(i);
            }
        }
        return null;
    }

    @Override
    public Space addSpace(Space space) {
        this.spaces.add(space);
        return this.spaces.get(this.spaces.size() - 1);
    }

    @Override
    public Space getOrphanSpace() {
        return getSpaceByName("orphans");
    }

    @Override
    public Space addToOrphansSpace(String memberId) {
        return getSpaceByName("orphans");
    }

    @Override
    public Space deleteSpace(String id) {
        return null;
    }

    @Override
    public List<String> getSpaceNamesOfMember(String memberId) {
        return null;
    }
}
