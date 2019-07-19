/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 *******************************************************************************/

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityMappingDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilityMappingSet;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MockFragilityMappingDAO implements IFragilityMappingDAO {
    private List<FragilityMappingSet> mappingSets = new ArrayList<>();

    @Override
    public void initialize() {
        URL mappingPath = this.getClass().getClassLoader().getResource("building_mapping.json");

        try {
            FragilityMappingSet mappingSet = new ObjectMapper().readValue(mappingPath, new TypeReference<FragilityMappingSet>() {});
            this.mappingSets = Arrays.asList(mappingSet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<FragilityMappingSet> getMappingSets() {
        return mappingSets;
    }

    @Override
    public Optional<FragilityMappingSet> getMappingSetById(String id) {
        return Optional.of(this.mappingSets.get(0));
    }

    @Override
    public List<FragilityMappingSet> queryMappingSets(Map<String, String> queryMap) {
        return null;
    }

    @Override
    public String saveMappingSet(FragilityMappingSet mappingSet) {
        this.mappingSets.add(mappingSet);
        return this.mappingSets.get(0).getId();
    }


}
