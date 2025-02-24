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
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.MappingSet;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MockFragilityMappingDAO implements IMappingDAO {
    private List<MappingSet> mappingSets = new ArrayList<>();

    @Override
    public void initialize() {
        URL mappingPath = this.getClass().getClassLoader().getResource("building_mapping.json");

        try {
            MappingSet mappingSet = new ObjectMapper().readValue(mappingPath, new TypeReference<MappingSet>() {
            });
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
    public MappingSet deleteMappingSetById(String id) {
        return this.mappingSets.get(0);
    }

    @Override
    public Boolean isCurvePresentInMappings(String id) {
        return true;
    }

    @Override
    public int getMappingCountByCreator(String creator) {
        return 0;
    }

    @Override
    public List<MappingSet> queryMappingSets(Map<String, String> queryMap) {
        return null;
    }

    @Override
    public String saveMappingSet(MappingSet mappingSet) {
        this.mappingSets.add(mappingSet);
        return this.mappingSets.get(0).getId();
    }

    @Override
    public List<MappingSet> searchMappings(String text, String mappingType) {
        return Arrays.asList();
    }
}
