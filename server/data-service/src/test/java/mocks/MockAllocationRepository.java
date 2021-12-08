/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.common.dao.IAllocationRepository;
import edu.illinois.ncsa.incore.common.models.Allocation;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockAllocationRepository implements IAllocationRepository {
    private final Datastore mockAllocationStore;
    private List<Allocation> allocations = new ArrayList<>();

    public MockAllocationRepository() {
        this.mockAllocationStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL allocationsPath = this.getClass().getClassLoader().getResource("json/allocations.json");

        try {
            this.allocations = new ObjectMapper().readValue(allocationsPath, new TypeReference<List<Allocation>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockAllocationStore.find(Allocation.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.allocations);
    }

    @Override
    public List<Allocation> getAllAllocations(){
        return this.allocations;
    }

    @Override
    public Allocation getAllocationById(String id) {
        return null;
    }

    @Override
    public Allocation getAllocationBySpaceId(String spaceId) {
        for (Allocation allocation : this.allocations) {
            if (allocation.getSpaceId().equals(spaceId)) {
                return allocation;
            }
        }
        return null;
    }
}
