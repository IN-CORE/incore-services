/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - based on Fragility service class
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import dev.morphia.morphia.Datastore;
import dev.morphia.morphia.Morphia;

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
