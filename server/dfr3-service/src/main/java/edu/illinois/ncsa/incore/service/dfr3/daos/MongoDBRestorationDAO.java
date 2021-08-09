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
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.service.dfr3.models.RestorationSet;
import org.bson.types.ObjectId;

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
        return this.dataStore.find(RestorationSet.class).iterator().toList();
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

        RestorationSet restorationSet = this.dataStore.find(RestorationSet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (restorationSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(restorationSet);
        }
    }

    @Override
    public RestorationSet deleteRestorationSetById(String id) {
        RestorationSet restorationSet = this.dataStore.find(RestorationSet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (restorationSet == null) {
            return null;
        } else {
            Query<RestorationSet> query = this.dataStore.find(RestorationSet.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
    }

    @Override
    public List<RestorationSet> searchRestorations(String text) {
        Query<RestorationSet> query = this.dataStore.find(RestorationSet.class).filter(
            Filters.or(
                Filters.regex("legacyId").pattern(text).caseInsensitive(),
                Filters.regex("hazardType").pattern(text).caseInsensitive(),
                Filters.regex("inventoryType").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive(),
                Filters.regex("authors").pattern(text).caseInsensitive()));
        List<RestorationSet> sets = query.iterator().toList();

        return sets;
    }

    @Override
    public List<RestorationSet> queryRestorations(String attributeType, String attributeValue) {
        List<RestorationSet> sets = this.dataStore.find(RestorationSet.class)
            .filter(Filters.eq(attributeType, attributeValue)).iterator()
            .toList();

        return sets;
    }

    @Override
    public List<RestorationSet> queryRestorations(Map<String, String> queryMap) {
        // TODO not sure if it works
        Query<RestorationSet> query = this.dataStore.find(RestorationSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        List<RestorationSet> sets = query.iterator().toList();

        return sets;
    }
}
