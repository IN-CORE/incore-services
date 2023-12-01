/*
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.semantics.daos;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import edu.illinois.ncsa.incore.service.semantics.model.Type;

public abstract class MongoDAO {
    protected MongoClientURI mongoClientURI;
    protected Datastore typeDataStore;
    protected String databaseName;

    public MongoDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    public void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Type.class);
        morphiaStore.ensureIndexes();
        this.typeDataStore = morphiaStore;
    }

    public Datastore getTypeDataStore() {
        return this.typeDataStore;
    }

}
