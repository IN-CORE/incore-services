/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.daos;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.dfr3.models.FragilitySet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBFragilityDAO extends MongoDAO implements IFragilityDAO {

    public MongoDBFragilityDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(FragilitySet.class);
    }

    @Override
    public List<FragilitySet> getFragilities() {
        return this.dataStore.createQuery(FragilitySet.class).asList();
    }

    @Override
    public String saveFragility(FragilitySet fragilitySet) {
        if (fragilitySet == null) {
            throw new IllegalArgumentException();
        } else {
            // the save method mutates the fragilitySet object with an document id
            String id = this.dataStore.save(fragilitySet).getId().toString();

            return id;
        }

    }

    @Override
    public Optional<FragilitySet> getFragilitySetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

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
            query.criteria("description").containsIgnoreCase(text),
            query.criteria("authors").containsIgnoreCase(text));

        List<FragilitySet> sets = query.asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class)
            .filter(attributeType, attributeValue)
            .asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap) {
        Query<FragilitySet> query = this.dataStore.createQuery(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<FragilitySet> sets = query.asList();

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
}
