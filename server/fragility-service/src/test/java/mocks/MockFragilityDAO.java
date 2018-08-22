/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import org.bson.types.ObjectId;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class MockFragilityDAO implements IFragilityDAO {
    private Datastore mockDataStore;
    private List<FragilitySet> fragilitySets = new ArrayList<>();

    public MockFragilityDAO() {
        this.mockDataStore = Mockito.mock(Datastore.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Override
    public void initialize() {
        URL fragilityPath = this.getClass().getClassLoader().getResource("fragility.json");

        try {
            this.fragilitySets = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<FragilitySet>>() {});
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.createQuery(FragilitySet.class)
                                  .limit(Mockito.any(Integer.class))
                                  .asList())
               .thenReturn(this.fragilitySets);

        // generate mock key object with uuid once successfully saved
        Answer<Key<FragilitySet>> ans = new Answer<Key<FragilitySet>>() {
            @Override
            public Key<FragilitySet> answer(InvocationOnMock invocation) throws Throwable {
                return new Key(FragilitySet.class, "newdataset", UUID.randomUUID());
            }
        };
        Mockito.when(mockDataStore.save(Mockito.any(FragilitySet.class))).thenAnswer(ans);
    }

    @Override
    public List<FragilitySet> getCachedFragilities() {
        return this.fragilitySets;
    }

    @Override
    public void saveFragility(FragilitySet fragilitySet) {
        // mutate fragilitySet object with this id
        try {
            Field field = FragilitySet.class.getDeclaredField("id");
            field.setAccessible(true);

            try {
                field.set(fragilitySet, new ObjectId());
            } catch (IllegalAccessException e) {
                // do nothing
            }
        } catch (NoSuchFieldException e) {
            // do nothing
        }
    }


    @Override
    public Optional<FragilitySet> getFragilitySetById(String id) {
        FragilitySet fragilitySet = this.mockDataStore.get(FragilitySet.class, id);
        return Optional.of(fragilitySet);
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
