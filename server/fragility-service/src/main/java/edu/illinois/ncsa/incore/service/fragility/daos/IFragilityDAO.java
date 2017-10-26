/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.daos;

import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import org.mongodb.morphia.Datastore;

import java.util.List;
import java.util.Map;

public interface IFragilityDAO {
    void initialize();
    List<FragilitySet> getFragilities();
    FragilitySet getById(String id);
    List<FragilitySet> searchFragilities(String text);
    Datastore getDataStore();
    List<FragilitySet> queryFragilities(String attributeType, String attributeValue);
    List<FragilitySet> queryFragilities(Map<String, String> typeValueMap);
    List<FragilitySet> queryFragilityAuthor(String author);
}
