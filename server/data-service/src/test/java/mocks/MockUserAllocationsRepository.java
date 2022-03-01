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
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockUserAllocationsRepository implements IUserAllocationsRepository {
    private final Datastore mockAllocationStore;
    private List<UserAllocations> userAllocations = new ArrayList<>();

    public MockUserAllocationsRepository() {
        this.mockAllocationStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL allocationsPath = this.getClass().getClassLoader().getResource("json/allocations.json");

        try {
            this.userAllocations = new ObjectMapper().readValue(allocationsPath, new TypeReference<List<UserAllocations>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockAllocationStore.find(UserAllocations.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.userAllocations);
    }

    @Override
    public List<UserAllocations> getAllAllocations(){
        return this.userAllocations;
    }

    @Override
    public UserAllocations getAllocationById(String id) {
        return null;
    }

    @Override
    public UserAllocations getAllocationByUsername(String username) {
        for (UserAllocations userAllocations : this.userAllocations) {
            if (userAllocations.getUsername().equals(username)) {
                return userAllocations;
            }
        }
        return null;
    }

    @Override
    public UserAllocations addAllocation(UserAllocations allocation) { return null; }
}
