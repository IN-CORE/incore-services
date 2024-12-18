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

import dev.morphia.Datastore;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;

import java.util.List;

public interface IEarthquakeRepository {
    void initialize();

    Earthquake getEarthquakeById(String id);

    Earthquake addEarthquake(Earthquake earthquake);

    Earthquake deleteEarthquakeById(String id);

    List<Earthquake> getEarthquakes();

    List<Earthquake> searchEarthquakes(String text);

    List<Earthquake> getEarthquakesByCreator(String creator);

    int getEarthquakesCountByCreator(String creator);

    Datastore getDataStore();
}
