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

import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

import java.util.List;

public class MockRepository implements IEarthquakeRepository {
    private static final Logger log = Logger.getLogger(MockRepository.class);
    private Datastore mockDataStore;

    public MockRepository() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
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
    public Datastore getDataStore() {
        return this.mockDataStore;
    }


}
