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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;

public abstract class MongoDAO {
    protected MongoClientURI mongoClientURI;
    protected MongoCollection typeDataStore;
    protected String databaseName;

    public MongoDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    public void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);
        this.typeDataStore = client.getDatabase(databaseName).getCollection("Type");
    }

    public MongoCollection getTypeDataStore() {
        return this.typeDataStore;
    }


}
