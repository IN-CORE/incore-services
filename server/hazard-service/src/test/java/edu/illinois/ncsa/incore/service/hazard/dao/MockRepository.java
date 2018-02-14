/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.ScenarioTornado;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MockRepository implements IRepository {
    private static final Logger log = Logger.getLogger(MockRepository.class);
    private Datastore mockDataStore;
    private List<ScenarioTornado> scenarioTornadoes = new ArrayList<ScenarioTornado>();

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL tornadoPath = this.getClass().getClassLoader().getResource("json/scenarioTornadoes.json");
        try {
            this.scenarioTornadoes = new ObjectMapper().readValue(tornadoPath, new TypeReference<List<ScenarioTornado>>() {
            });

            Mockito.when(mockDataStore.createQuery(ScenarioTornado.class).limit(Mockito.any(Integer.class)).asList()).thenReturn(this.scenarioTornadoes);

        } catch (IOException e) {
            log.error("Error reading tornadoes", e);
        }
    }

    @Override
    public ScenarioEarthquake getScenarioEarthquakeById(String id) {
        return null;
    }

    @Override
    public ScenarioEarthquake addScenarioEarthquake(ScenarioEarthquake scenarioEarthquake) {
        return null;
    }

    @Override
    public List<ScenarioEarthquake> getScenarioEarthquakes() {
        return null;
    }

    @Override
    public ScenarioTornado getScenarioTornadoById(String id) {
        for (int index = 0; index < this.scenarioTornadoes.size(); index++) {
            if (this.scenarioTornadoes.get(index).getId().equalsIgnoreCase(id)) {
                return this.scenarioTornadoes.get(index);
            }
        }
        return null;
    }

    @Override
    public ScenarioTornado addScenarioTornado(ScenarioTornado scenarioTornado) {
        return null;
    }

    @Override
    public List<ScenarioTornado> getScenarioTornadoes() {
        return this.scenarioTornadoes;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }


}
