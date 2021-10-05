/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import edu.illinois.ncsa.incore.service.maestro.daos.IPlaybookDAO;
import edu.illinois.ncsa.incore.service.maestro.models.Playbook;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IPlaybookDAO {
    private final Datastore mockDataStore;
    private List<Playbook> playbooks = new ArrayList<>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL playbooksPath = this.getClass().getClassLoader().getResource("playbooks.json");

        try {
            this.playbooks = new ObjectMapper().readValue(playbooksPath, new TypeReference<List<Playbook>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.find(Playbook.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
            .thenReturn(this.playbooks);

    }

    @Override
    public List<Playbook> getAllPlaybooks() {
        return this.playbooks;
    }


    @Override
    public Playbook getPlaybookById(String id) {
        for (int i = 0; i < this.playbooks.size(); i++) {
            if (this.playbooks.get(i).getId().equalsIgnoreCase(id)) {
                return this.playbooks.get(i);
            }
        }
        return null;
    }

}
