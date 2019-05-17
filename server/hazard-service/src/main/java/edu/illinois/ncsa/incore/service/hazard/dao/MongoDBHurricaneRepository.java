/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneWindfields;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.*;

public class MongoDBHurricaneRepository implements IHurricaneRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;

    // TODO I believe we can just use the URI and remove this later
    public MongoDBHurricaneRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBHurricaneRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBHurricaneRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);

        Set<Class> classesToMap = new HashSet<>();
        Morphia morphia = new Morphia(classesToMap);
        classesToMap.add(Earthquake.class);
        Datastore morphiaStore = morphia.createDatastore(client, mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public HurricaneWindfields addHurricane(HurricaneWindfields hurricane) {
        String id = this.dataStore.save(hurricane).getId().toString();
        return getHurricaneById(id);
    }

    @Override
    public HurricaneWindfields getHurricaneById(String id) {
        return this.dataStore.get(HurricaneWindfields.class, new ObjectId(id));
    }

    @Override
    public List<HurricaneWindfields> getHurricanes() {
        List<HurricaneWindfields> hurricaneWindfields = this.dataStore.createQuery(HurricaneWindfields.class).asList();
        return hurricaneWindfields;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<HurricaneWindfields> queryHurricanes(String attributeType, String attributeValue) {
        List<HurricaneWindfields> hurricanes = this.dataStore.createQuery(HurricaneWindfields.class)
            .filter(attributeType, attributeValue)
            .asList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> queryHurricanes(Map<String, String> queryMap) {
        Query<HurricaneWindfields> query = this.dataStore.createQuery(HurricaneWindfields.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<HurricaneWindfields> hurricanes = query.asList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> searchHurricanes(String text) {
        Query<HurricaneWindfields> query = this.dataStore.createQuery(HurricaneWindfields.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        List<HurricaneWindfields> hurricanes = query.asList();

        return hurricanes;
    }
}
