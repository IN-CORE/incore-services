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
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneDataset;
import org.bson.types.ObjectId;

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
        classesToMap.add(Hurricane.class);
        Datastore morphiaStore = morphia.createDatastore(client, mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public Hurricane addHurricane(Hurricane hurricane) {
        String id = this.dataStore.save(hurricane).getId().toString();
        return getHurricaneById(id);
    }

    @Override
    public Hurricane deleteHurricaneById(String id) {
        Hurricane hurricane = this.dataStore.get(Hurricane.class, new ObjectId(id));
        if (hurricane != null) {
            Query<Hurricane> query = this.dataStore.createQuery(Hurricane.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        }
        return null;
    }

    @Override
    public Hurricane getHurricaneById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Hurricane hurricane = this.dataStore.get(HurricaneDataset.class, new ObjectId(id));
        // TODO this will need to be updated if there are model based hurricanes

        return hurricane;
    }

    @Override
    public List<Hurricane> getHurricanes() {
        List<Hurricane> hurricanes = new LinkedList<>();
        List<HurricaneDataset> hurricaneDatasets = this.dataStore.createQuery(HurricaneDataset.class).asList();
        hurricanes.addAll(hurricaneDatasets);
        // TODO this will need to be updated if there are model based hurricanes

        return hurricanes;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<Hurricane> queryHurricanes(String attributeType, String attributeValue) {
        List<Hurricane> hurricanes = this.dataStore.createQuery(Hurricane.class)
            .filter(attributeType, attributeValue)
            .asList();

        return hurricanes;
    }

    @Override
    public List<Hurricane> queryHurricanes(Map<String, String> queryMap) {
        Query<Hurricane> query = this.dataStore.createQuery(Hurricane.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<Hurricane> hurricanes = query.asList();

        return hurricanes;
    }

    @Override
    public List<Hurricane> searchHurricanes(String text) {
        Query<Hurricane> query = this.dataStore.createQuery(Hurricane.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        List<Hurricane> hurricanes = query.asList();

        return hurricanes;
    }
}
