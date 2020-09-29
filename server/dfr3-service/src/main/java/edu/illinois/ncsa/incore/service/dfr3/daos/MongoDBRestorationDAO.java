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
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBRestorationDAO extends MongoDAO implements IRestorationDAO {

    public MongoDBRestorationDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(RestorationSet.class);
    }

    @Override
    public List<RestorationSet> getRestorations() {
        return this.dataStore.createQuery(RestorationSet.class).asList();
    }

    @Override
    public String saveRestoration(RestorationSet restorationSet) {
        if (restorationSet == null) {
            throw new IllegalArgumentException();
        } else {
            // the save method mutates the fragilitySet object with an document id
            String id = this.dataStore.save(restorationSet).getId().toString();

            return id;
        }

    }

    @Override
    public Optional<RestorationSet> getRestorationSetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        RestorationSet restorationSet = this.dataStore.get(RestorationSet.class, new ObjectId(id));

        if (restorationSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(restorationSet);
        }
    }

    @Override
    public List<RestorationSet> searchRestorations(String text) {
        Query<RestorationSet> query = this.dataStore.createQuery(RestorationSet.class);

        query.or(query.criteria("legacyId").containsIgnoreCase(text),
            query.criteria("hazardType").containsIgnoreCase(text),
            query.criteria("inventoryType").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text),
            query.criteria("authors").containsIgnoreCase(text));

        List<RestorationSet> sets = query.asList();

        return sets;
    }

    @Override
    public List<RestorationSet> queryRestorations(String attributeType, String attributeValue) {
        List<RestorationSet> sets = this.dataStore.createQuery(RestorationSet.class)
            .filter(attributeType, attributeValue)
            .asList();

        return sets;
    }

    @Override
    public List<RestorationSet> queryRestorations(Map<String, String> queryMap) {
        Query<RestorationSet> query = this.dataStore.createQuery(RestorationSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<RestorationSet> sets = query.asList();

        return sets;
    }
}
