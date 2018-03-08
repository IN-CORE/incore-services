/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert, Chen Wang
 */

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockFragilityDAO implements IFragilityDAO {
    private Datastore mockDataStore;
    private List<FragilitySet> fragilitySets = new ArrayList<>();

    public MockFragilityDAO() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL fragilityPath = this.getClass().getClassLoader().getResource("json/fragility.json");

        try {
            this.fragilitySets = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<FragilitySet>>(){});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.createQuery(FragilitySet.class)
                                  .limit(Mockito.any(Integer.class))
                                  .asList())
               .thenReturn(this.fragilitySets);
    }

    @Override
    public List<FragilitySet> getFragilities() {
        return this.fragilitySets;
    }

    @Override
    public FragilitySet saveFragility(FragilitySet fragilitySet) {
        if (fragilitySet == null){
            return null;
        } else {
            this.mockDataStore.save(fragilitySet);
            
            return fragilitySet;
        }
        
    }

    @Override
    public FragilitySet getById(String id) {
        FragilitySet fragilitySet = this.mockDataStore.get(FragilitySet.class, id);
        return fragilitySet;
    }

    @Override
    public List<FragilitySet> searchFragilities(String text) {
        Query<FragilitySet> query = this.mockDataStore.createQuery(FragilitySet.class);

        query.or(query.criteria("demandType").containsIgnoreCase(text),
                 query.criteria("legacyId").containsIgnoreCase(text),
                 query.criteria("hazardType").containsIgnoreCase(text),
                 query.criteria("inventoryType").containsIgnoreCase(text),
                 query.criteria("authors").containsIgnoreCase(text));

        List<FragilitySet> sets = query.limit(100).asList();

        return sets;
    }

    @Override
    public Datastore getDataStore() {
        return this.mockDataStore;
    }

    @Override
    public List<FragilitySet> queryFragilities(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.mockDataStore.createQuery(FragilitySet.class)
                                                .filter(attributeType, attributeValue)
                                                .limit(100)
                                                .asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap, int offset, int limit) {
        Query<FragilitySet> query = this.mockDataStore.createQuery(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<FragilitySet> sets = query.offset(offset).limit(limit).asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilityAuthor(String author) {
        List<FragilitySet> sets = this.mockDataStore.createQuery(FragilitySet.class)
                                                .field("authors")
                                                .contains(author)
                                                .asList();

        return sets;
    }
}
