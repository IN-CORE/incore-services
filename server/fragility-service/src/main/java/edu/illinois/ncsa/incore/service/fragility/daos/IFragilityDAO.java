/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.daos;

import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFragilityDAO {
    void initialize();
    List<FragilitySet> getFragilities();
    String saveFragility(FragilitySet fragilitySet);
    Optional<FragilitySet> getFragilitySetById(String id);
    List<FragilitySet> searchFragilities(String text);
    List<FragilitySet> queryFragilities(String attributeType, String attributeValue);
    List<FragilitySet> queryFragilities(Map<String, String> typeValueMap);
    List<FragilitySet> queryFragilityAuthor(String author);
}
