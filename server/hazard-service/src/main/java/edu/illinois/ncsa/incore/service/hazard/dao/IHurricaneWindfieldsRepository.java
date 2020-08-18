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

import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneWindfields;
import dev.morphia.Datastore;

import java.util.Map;
import java.util.List;

public interface IHurricaneWindfieldsRepository {
    void initialize();

    HurricaneWindfields getHurricaneWindfieldsById(String id);

    HurricaneWindfields addHurricaneWindfields(HurricaneWindfields hurricane);

    HurricaneWindfields deleteHurricaneWindfieldsById(String id);

    List<HurricaneWindfields> getHurricaneWindfields();

    List<HurricaneWindfields> searchHurricaneWindfields(String text);

    Datastore getDataStore();

    List<HurricaneWindfields> queryHurricaneWindfields(String attributeType, String attributeValue);
    List<HurricaneWindfields> queryHurricaneWindfields(Map<String, String> queryMap);

}
