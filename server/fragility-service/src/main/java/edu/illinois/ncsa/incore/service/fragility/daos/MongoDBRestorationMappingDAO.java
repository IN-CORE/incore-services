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
import edu.illinois.ncsa.incore.service.fragility.models.RestorationMappingSet;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MongoDBRestorationMappingDAO extends MongoDAO implements IRestorationMappingDAO {

    public MongoDBRestorationMappingDAO(MongoClientURI mongoClientURI) {
        super(mongoClientURI);
    }

    @Override
    public void initialize() {
        super.initializeDataStore(RestorationMappingSet.class);
    }

    @Override
    public List<RestorationMappingSet> getMappingSets() {
        return this.dataStore.createQuery(RestorationMappingSet.class).asList();
    }

    @Override
    public Optional<RestorationMappingSet> getMappingSetById(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }

        RestorationMappingSet mappingSet = this.dataStore.get(RestorationMappingSet.class, new ObjectId(id));

        if (mappingSet == null) {
            return Optional.empty();
        } else {
            return Optional.of(mappingSet);
        }
    }

    @Override
    public String saveMappingSet(RestorationMappingSet mappingSet) {
        if (mappingSet == null) {
            throw new IllegalArgumentException();
        } else {
            String id = this.dataStore.save(mappingSet).getId().toString();
            return id;
        }
    }

    @Override
    public List<RestorationMappingSet> queryMappingSets(Map<String, String> queryMap) {
        Query<RestorationMappingSet> query = this.dataStore.createQuery(RestorationMappingSet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<RestorationMappingSet> mappingSets = query.asList();

        return mappingSets;
    }
}
