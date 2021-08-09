/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import org.apache.log4j.Logger;
import org.mockito.Mockito;
import dev.morphia.Datastore;

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
    public Earthquake getEarthquakeById(String id) {
        return null;
    }

    @Override
    public Earthquake addEarthquake(Earthquake scenarioEarthquake) {
        return null;
    }

    @Override
    public Earthquake deleteEarthquakeById(String id) {
        return null;
    }

    @Override
    public List<Earthquake> getEarthquakes() {
        return null;
    }

    @Override
    public List<Earthquake> searchEarthquakes(String text) {
        return null;
    }

    @Override
    public List<Earthquake> getEarthquakesByCreator(String creator) {
        return null;
    }

    @Override
    public int getEarthquakesCountByCreator(String creator) {
        return 0;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }


}
