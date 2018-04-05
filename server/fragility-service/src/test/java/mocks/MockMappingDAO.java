/*
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd
 */

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.fragility.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.fragility.models.MappingSet;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MockMappingDAO implements IMappingDAO {
    private List<MappingSet> mappingSets = new ArrayList<>();

    @Override
    public void initialize() {
        URL mappingPath = this.getClass().getClassLoader().getResource("building_mapping.json");

        try {
            MappingSet mappingSet = new ObjectMapper().readValue(mappingPath, new TypeReference<MappingSet>() {});
            this.mappingSets = Arrays.asList(mappingSet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<MappingSet> getMappingSets() {
        return mappingSets;
    }

    @Override
    public Optional<MappingSet> getMappingSetById(String id) {
        return Optional.of(this.mappingSets.get(0));
    }

    @Override
    public List<MappingSet> queryMappingSets(Map<String, String> queryMap, int offset, int limit) {
        return null;
    }

    @Override
    public void saveMappingSet(MappingSet mappingSet) {
        this.mappingSets.add(mappingSet);
    }
}
