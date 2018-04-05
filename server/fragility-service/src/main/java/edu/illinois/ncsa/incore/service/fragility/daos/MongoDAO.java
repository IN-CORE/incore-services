/*
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.fragility.daos;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class MongoDAO {
    protected MongoClientURI mongoClientURI;
    protected Datastore dataStore;
    protected String databaseName;

    public MongoDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    public void initializeDataStore(Class... classes) {
        MongoClient client = new MongoClient(mongoClientURI);


        Set<Class> classesToMap = new HashSet<>(Arrays.asList(classes));

        Morphia morphia = new Morphia(classesToMap);

        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    public Datastore getDataStore() {
        return this.dataStore;
    }
}
