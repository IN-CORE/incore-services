/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
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
import edu.illinois.ncsa.incore.common.models.Allocation;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;


public class MongoAllocationDBRepository implements IAllocationRepository {
    private final String ALLOCATION_FIELD_SPACE_ID = "space_id";
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private Datastore dataStore;

    private final Logger logger = Logger.getLogger(MongoAllocationDBRepository.class);

    public MongoAllocationDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Allocation> getAllAllocations() {
        return this.dataStore.find(Allocation.class).iterator().toList();
    }

    public Allocation getAllocationById(String id) {
        return this.dataStore.find(Allocation.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public Allocation getAllocationBySpaceId(String spaceId) {
        List<Allocation> allocationList = new ArrayList<>();
        allocationList = this.dataStore.find(Allocation.class).iterator().toList();
        Query<Allocation> allocationQuery = this.dataStore.find(Allocation.class);
        Allocation foundAllocation = allocationQuery.filter(Filters.eq(ALLOCATION_FIELD_SPACE_ID, spaceId)).first();

        return foundAllocation;
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Allocation.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

}
