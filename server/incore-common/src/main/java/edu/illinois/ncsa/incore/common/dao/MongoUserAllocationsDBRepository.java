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
import edu.illinois.ncsa.incore.common.models.UserAllocations;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.List;


public class MongoUserAllocationsDBRepository implements IUserAllocationsRepository {
    private final String ALLOCATION_FIELD_USERNAME = "username";
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private final Logger logger = Logger.getLogger(MongoUserAllocationsDBRepository.class);

    public MongoUserAllocationsDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<UserAllocations> getAllAllocations() {
        return this.dataStore.find(UserAllocations.class).iterator().toList();
    }

    public UserAllocations getAllocationById(String id) {
        return this.dataStore.find(UserAllocations.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public UserAllocations getAllocationByUsername(String username) {
        Query<UserAllocations> allocationQuery = this.dataStore.find(UserAllocations.class);
        UserAllocations foundUserAllocations = allocationQuery.filter(Filters.eq(ALLOCATION_FIELD_USERNAME, username)).first();

        return foundUserAllocations;
    }

    public UserAllocations addAllocation(UserAllocations allocation) {
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
        morphiaStore.getMapper().map(UserAllocations.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

}
