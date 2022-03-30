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
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import edu.illinois.ncsa.incore.common.models.UserFinalQuota;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockUserFinalQuotaRepository implements IUserFinalQuotaRepository {
    private final Datastore mockQuotaStore;
    private List<UserFinalQuota> userFinalQuotas = new ArrayList<>();

    public MockUserFinalQuotaRepository() {
        this.mockQuotaStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL quotasPath = this.getClass().getClassLoader().getResource("json/allocations.json");

        try {
            this.userFinalQuotas = new ObjectMapper().readValue(quotasPath, new TypeReference<List<UserFinalQuota>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockQuotaStore.find(UserFinalQuota.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.userFinalQuotas);
    }

    @Override
    public List<UserFinalQuota> getAllQuotas() {
        return this.userFinalQuotas;
    }

    @Override
    public UserFinalQuota getQuotaById(String id) {
        return null;
    }

    @Override
    public UserFinalQuota getQuotaByUsername(String username) {
        for (UserFinalQuota userFinalQuota : this.userFinalQuotas) {
            if (userFinalQuota.getUsername().equals(username)) {
                return userFinalQuota;
            }
        }
        return null;
    }
}
