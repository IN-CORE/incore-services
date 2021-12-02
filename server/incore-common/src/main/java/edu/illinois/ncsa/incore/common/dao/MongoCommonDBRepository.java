/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


public class MongoCommonDBRepository implements ICommonRepository {
    private final String databaseName;
    private final MongoClientURI mongoClientURI;
    private MongoCollection dataStore;

    private final Logger logger = Logger.getLogger(MongoCommonDBRepository.class);

    public MongoCommonDBRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Document> getAllDemandDefinitions() {
        return (List<Document>) this.dataStore.find().into(new ArrayList<Document>());
    }

    public Document getDemandDefinitionById(String id) {
        return (Document) this.dataStore.find(eq("_id", new ObjectId(id))).first();
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);
        this.dataStore = client.getDatabase(databaseName).getCollection("Demand");
    }
}
