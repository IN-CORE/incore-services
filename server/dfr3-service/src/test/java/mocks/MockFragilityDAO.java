/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/

package mocks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.service.dfr3.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.dfr3.models.FragilitySet;
import org.bson.types.ObjectId;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
            this.fragilitySets = new ObjectMapper().readValue(fragilityPath, new TypeReference<List<FragilitySet>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Mockito.when(mockDataStore.find(FragilitySet.class)
                .iterator(new FindOptions().limit(Mockito.any(Integer.class))).toList())
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
    public List<FragilitySet> getFragilities() {
        return this.fragilitySets;
    }

    @Override
    public String saveFragility(FragilitySet fragilitySet) {
        // mutate fragilitySet object with this id
        try {
            Field field = FragilitySet.class.getDeclaredField("id");
            field.setAccessible(true);

            try {
                field.set(fragilitySet, new ObjectId());
                return fragilitySet.getId();
            } catch (IllegalAccessException e) {
                // do nothing
            }
        } catch (NoSuchFieldException e) {
            // do nothing
        }
        return null;
    }


    @Override
    public Optional<FragilitySet> getFragilitySetById(String id) {
        FragilitySet fragilitySet = this.mockDataStore.find(FragilitySet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.of(fragilitySet);
    }

    @Override
    public FragilitySet deleteFragilitySetById(String id) {
        FragilitySet fragilitySet = this.mockDataStore.find(FragilitySet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (fragilitySet == null) {
            return null;
        } else {
            Query<FragilitySet> query = this.mockDataStore.find(FragilitySet.class);
            query.filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
    }

    @Override
    public List<FragilitySet> searchFragilities(String text) {
        Query<FragilitySet> query = this.mockDataStore.find(FragilitySet.class).filter(
            Filters.or(
                Filters.regex("demandType").pattern(text).caseInsensitive(),
                Filters.regex("legacyId").pattern(text).caseInsensitive(),
                Filters.regex("hazardType").pattern(text).caseInsensitive(),
                Filters.regex("inventoryType").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive(),
                Filters.regex("authors").pattern(text).caseInsensitive()));
        List<FragilitySet> sets = query.iterator(new FindOptions().limit(100)).toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.mockDataStore.find(FragilitySet.class)
            .filter(Filters.eq(attributeType, attributeValue))
            .iterator(new FindOptions().limit(100)).toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap) {
        Query<FragilitySet> query = this.mockDataStore.find(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        List<FragilitySet> sets = query.iterator().toList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilityAuthor(String author) {
        List<FragilitySet> sets = this.mockDataStore.find(FragilitySet.class)
            .filter(Filters.all("authors", author)).iterator()
            .toList();

        return sets;
    }
}
