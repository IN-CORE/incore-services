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
import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBRepairDAO extends MongoDAO implements IRepairDAO {

    public MongoDBRepairDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(RepairSet.class);
    }

    @Override
    public List<RepairSet> getRepairs() {
        return this.dataStore.find(RepairSet.class).iterator().toList();
    }

    @Override
    public String saveRepair(RepairSet repairSet) {
        if (repairSet == null) {
            throw new IllegalArgumentException();
        } else {
            // the save method mutates the fragilitySet object with an document id
            String id = this.dataStore.save(repairSet).getId().toString();

            return id;
        }

    }

    @Override
    public Optional<RepairSet> getRepairSetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        RepairSet repairSet = this.dataStore.find(RepairSet.class).filter(Filters.eq("_id", new ObjectId(id)))
            .first();

        if (repairSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(repairSet);
        }
    }

    @Override
    public RepairSet deleteRepairSetById(String id) {
        RepairSet repairSet = this.dataStore.find(RepairSet.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();

        if (repairSet == null) {
            return null;
        } else {
            Query<RepairSet> query = this.dataStore.find(RepairSet.class).filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
    }

    @Override
    public List<RepairSet> searchRepairs(String text) {
        Query<RepairSet> query = this.dataStore.find(RepairSet.class).filter(
            Filters.or(
                Filters.regex("legacyId").pattern(text).caseInsensitive(),
                Filters.regex("hazardType").pattern(text).caseInsensitive(),
                Filters.regex("inventoryType").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive(),
                Filters.regex("authors").pattern(text).caseInsensitive()));
        List<RepairSet> sets = query.iterator().toList();

        return sets;
    }

    @Override
    public List<RepairSet> queryRepairs(String attributeType, String attributeValue) {
        List<RepairSet> sets = this.dataStore.find(RepairSet.class)
            .filter(Filters.eq(attributeType, attributeValue)).iterator()
            .toList();

        return sets;
    }

    @Override
    public List<RepairSet> queryRepairs(Map<String, String> queryMap) {
        // TODO not sure this will work
        Query<RepairSet> query = this.dataStore.find(RepairSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        List<RepairSet> sets = query.iterator().toList();

        return sets;
    }
}
