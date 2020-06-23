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
import edu.illinois.ncsa.incore.service.dfr3.models.RepairSet;
import org.bson.types.ObjectId;
import dev.morphia.query.Query;

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
        return this.dataStore.createQuery(RepairSet.class).asList();
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

        RepairSet repairSet = this.dataStore.get(RepairSet.class, new ObjectId(id));

        if (repairSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(repairSet);
        }
    }

    @Override
    public List<RepairSet> searchRepairs(String text) {
        Query<RepairSet> query = this.dataStore.createQuery(RepairSet.class);

        query.or(query.criteria("legacyId").containsIgnoreCase(text),
            query.criteria("hazardType").containsIgnoreCase(text),
            query.criteria("inventoryType").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text),
            query.criteria("authors").containsIgnoreCase(text));

        List<RepairSet> sets = query.asList();

        return sets;
    }

    @Override
    public List<RepairSet> queryRepairs(String attributeType, String attributeValue) {
        List<RepairSet> sets = this.dataStore.createQuery(RepairSet.class)
            .filter(attributeType, attributeValue)
            .asList();

        return sets;
    }

    @Override
    public List<RepairSet> queryRepairs(Map<String, String> queryMap) {
        Query<RepairSet> query = this.dataStore.createQuery(RepairSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<RepairSet> sets = query.asList();

        return sets;
    }
}
