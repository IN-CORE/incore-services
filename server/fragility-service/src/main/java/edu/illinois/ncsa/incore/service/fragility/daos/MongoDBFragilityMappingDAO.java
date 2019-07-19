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
import edu.illinois.ncsa.incore.service.fragility.models.FragilityMappingSet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBFragilityMappingDAO extends MongoDAO implements IFragilityMappingDAO {

    public MongoDBFragilityMappingDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(FragilityMappingSet.class);
    }

    @Override
    public List<FragilityMappingSet> getMappingSets() {
        return this.dataStore.createQuery(FragilityMappingSet.class).asList();
    }

    @Override
    public Optional<FragilityMappingSet> getMappingSetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        FragilityMappingSet mappingSet = this.dataStore.get(FragilityMappingSet.class, new ObjectId(id));

        if (mappingSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(mappingSet);
        }
    }

    @Override
    public String saveMappingSet(FragilityMappingSet mappingSet) {
        if (mappingSet == null) {
            throw new IllegalArgumentException();
        } else {
            String id = this.dataStore.save(mappingSet).getId().toString();
            return id;
        }
    }

    @Override
    public List<FragilityMappingSet> queryMappingSets(Map<String, String> queryMap) {
        Query<FragilityMappingSet> query = this.dataStore.createQuery(FragilityMappingSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<FragilityMappingSet> mappingSets = query.asList();

        return mappingSets;
    }
}
