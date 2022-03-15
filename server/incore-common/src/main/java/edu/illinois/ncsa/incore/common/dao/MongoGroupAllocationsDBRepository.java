/*******************************************************************************
 * Copyright (c) 2021University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.dao;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.common.models.GroupAllocations;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.List;


public class MongoGroupAllocationsDBRepository implements IGroupAllocationsRepository {
    private final String GROUP_ALLOCATION_FIELD_USERNAME = "username";
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private final Logger logger = Logger.getLogger(MongoGroupAllocationsDBRepository.class);

    public MongoGroupAllocationsDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<GroupAllocations> getAllAllocations() {
        return this.dataStore.find(GroupAllocations.class).iterator().toList();
    }

    public GroupAllocations getAllocationById(String id) {
        return this.dataStore.find(GroupAllocations.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public GroupAllocations getAllocationByUsername(String username) {
        Query<GroupAllocations> allocationQuery = this.dataStore.find(GroupAllocations.class);
        GroupAllocations foundGroupAllocations = allocationQuery.filter(Filters.eq(GROUP_ALLOCATION_FIELD_USERNAME, username)).first();

        return foundGroupAllocations;
    }

    public GroupAllocations addAllocation(GroupAllocations allocation) {
        String id = (this.dataStore.save(allocation)).getId();
        return getAllocationById(id);
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(GroupAllocations.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

}
