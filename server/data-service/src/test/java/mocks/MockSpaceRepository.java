/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *   Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.models.Space;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockSpaceRepository implements ISpaceRepository {
    private final Datastore mockSpaceStore;
    private List<Space> spaces = new ArrayList<>();

    public MockSpaceRepository() {
        this.mockSpaceStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
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

        Mockito.when(mockSpaceStore.find(Space.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.spaces);
    }

    @Override
    public List<Space> getAllSpaces() {
        return this.spaces;
    }

    @Override
    public Space addSpace(Space space) {
        this.spaces.add(space);
        return space;
    }

    @Override
    public Space getSpaceById(String id) {
        return null;
    }

    @Override
    public Space getSpaceByName(String name) {
        for (Space space : this.spaces) {
            if (space.getMetadata().getName().equals(name)) {
                return space;
            }
        }
        return null;
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
