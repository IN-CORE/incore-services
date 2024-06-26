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
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;

import java.util.List;

public interface ITornadoRepository {
    void initialize();

    Tornado getTornadoById(String id);

    Tornado addTornado(Tornado tornado);

    Tornado deleteTornadoById(String id);

    List<Tornado> getTornadoes();

    List<Tornado> searchTornadoes(String text);

    List<Tornado> getTornadoesByCreator(String creator);

    int getTornadoesCountByCreator(String creator);

    Datastore getDataStore();
}
