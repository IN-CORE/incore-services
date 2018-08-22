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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import edu.illinois.ncsa.incore.service.fragility.models.MappingSet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.*;

public class MongoDBFragilityDAO extends MongoDAO implements IFragilityDAO {
    // Since the list of fragilities are currently few (at the moment) we can store them in memory
    private List<FragilitySet> fragilities;

    public MongoDBFragilityDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(FragilitySet.class);
        this.loadFragilities();
    }

    @Override
    public List<FragilitySet> getCachedFragilities() {
        if (this.fragilities == null || this.fragilities.isEmpty()) {
            this.loadFragilities();
        }

        return this.fragilities;
    }

    @Override
    public void saveFragility(FragilitySet fragilitySet) {
        if (fragilitySet == null) {
            throw new IllegalArgumentException();
        } else {
            // the save method mutates the fragilitySet object with an document id
            this.dataStore.save(fragilitySet);

            // make sure that this.fragilities get updated as well
            this.loadFragilities();
        }

    }

    @Override
    public Optional<FragilitySet> getFragilitySetById(String id) {
        FragilitySet fragilitySet = this.dataStore.get(FragilitySet.class, new ObjectId(id));

        if (fragilitySet == null) {
            return Optional.empty();
        } else {
            return Optional.of(fragilitySet);
        }
    }

    @Override
    public List<FragilitySet> searchFragilities(String text) {
        Query<FragilitySet> query = this.dataStore.createQuery(FragilitySet.class);

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
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class)
                                                .filter(attributeType, attributeValue)
                                                .limit(100)
                                                .asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap, int offset, int limit) {
        Query<FragilitySet> query = this.dataStore.createQuery(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<FragilitySet> sets = query.offset(offset).limit(limit).asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilityAuthor(String author) {
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class)
                                                .field("authors")
                                                .contains(author)
                                                .asList();

        return sets;
    }

    private void loadFragilities() {
        List<FragilitySet> fragilitySets = this.dataStore.createQuery(FragilitySet.class).asList();
        this.fragilities = fragilitySets;
    }
}
