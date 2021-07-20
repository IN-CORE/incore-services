/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import dev.morphia.Datastore;
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;

import java.util.List;

public interface IFloodRepository {
    void initialize();

    Flood getFloodById(String id);

    Flood addFlood(Flood flood);

    Flood deleteFloodById(String id);

    List<Flood> getFloods();

    List<Flood> searchFloods(String text);

    List<Flood> getFloodsByCreator(String creator);

    int getFloodsCountByCreator(String creator);

    Datastore getDataStore();

}
