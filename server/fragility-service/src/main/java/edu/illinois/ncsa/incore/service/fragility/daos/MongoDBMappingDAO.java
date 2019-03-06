/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.fragility.daos;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.fragility.models.MappingSet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBMappingDAO extends MongoDAO implements IMappingDAO {
    // Since the list of mappings are currently few (at the moment) we can store them in memory
    private List<MappingSet> mappingSets;

    public MongoDBMappingDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(MappingSet.class);
        this.loadMappingSets();
    }

    @Override
    public List<MappingSet> getMappingSets() {
        if (this.mappingSets == null || this.mappingSets.isEmpty()) {
            this.loadMappingSets();
        }

        return this.mappingSets;
    }

    @Override
    public Optional<MappingSet> getMappingSetById(String id) {
        MappingSet mappingSet = this.dataStore.get(MappingSet.class, new ObjectId(id));

        if (mappingSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(mappingSet);
        }
    }

    @Override
    public void saveMappingSet(MappingSet mappingSet) {
        this.dataStore.save(mappingSet);
    }

    @Override
    public List<MappingSet> queryMappingSets(Map<String, String> queryMap, int offset, int limit) {
        Query<MappingSet> query = this.dataStore.createQuery(MappingSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<MappingSet> mappingSets = query.offset(offset).limit(limit).asList();

        return mappingSets;
    }

    private void loadMappingSets() {
        List<MappingSet> mappingSets = this.dataStore.createQuery(MappingSet.class).asList();
        this.mappingSets = mappingSets;
    }
}
