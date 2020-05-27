/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.TornadoModel;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockTornadoRepository implements ITornadoRepository {
    private static final Logger log = Logger.getLogger(MockRepository.class);
    private Datastore mockDataStore;
    private List<Tornado> tornadoes = new ArrayList<>();

    public MockTornadoRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL tornadoPath = this.getClass().getClassLoader().getResource("json/scenarioTornadoes.json");
        try {
            List<TornadoModel> tornadoModels = new ObjectMapper().readValue(tornadoPath, new TypeReference<List<TornadoModel>>() {
            });
            this.tornadoes.addAll(tornadoModels);

            Mockito.when(mockDataStore.createQuery(Tornado.class).asList(new FindOptions().limit(Mockito.any(Integer.class)))).thenReturn(this.tornadoes);
        } catch (IOException e) {
            log.error("Error reading tornadoes", e);
        }
    }

    @Override
    public Tornado getTornadoById(String id) {
        for (int index = 0; index < this.tornadoes.size(); index++) {
            if (this.tornadoes.get(index).getId().equalsIgnoreCase(id)) {
                return this.tornadoes.get(index);
            }
        }
        return null;
    }

    @Override
    public Tornado addTornado(Tornado tornado) {
        return null;
    }

    @Override
    public Tornado deleteTornadoById(String id) {
        return null;
    }

    @Override
    public List<Tornado> getTornadoes() {
        System.out.println(this.tornadoes.size());
        return this.tornadoes;
    }

    @Override
    public List<Tornado> searchTornadoes(String text) {
        return null;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }


}
