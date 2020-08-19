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
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;
import edu.illinois.ncsa.incore.service.hazard.models.flood.FloodDataset;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoDBFloodRepository implements IFloodRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;

    // TODO I believe we can just use the URI and remove this later
    public MongoDBFloodRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBFloodRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBFloodRepository(MongoClientURI mongoClientURI) {
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
        classesToMap.add(Flood.class);
        Datastore morphiaStore = morphia.createDatastore(client, mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public Flood addFlood(Flood flood) {
        String id = this.dataStore.save(flood).getId().toString();
        return getFloodById(id);
    }

    @Override
    public Flood deleteFloodById(String id) {
        Flood flood = this.dataStore.get(FloodDataset.class, new ObjectId(id));
        if (flood != null) {
            Query<FloodDataset> query = this.dataStore.createQuery(FloodDataset.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        }
        return null;
    }

    @Override
    public Flood getFloodById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Flood flood = this.dataStore.get(FloodDataset.class, new ObjectId(id));
        // TODO this will need to be updated if there are model based floods

        return flood;
    }

    @Override
    public List<Flood> getFloods() {
        List<Flood> floods = new LinkedList<>();
        List<FloodDataset> floodDatasets = this.dataStore.createQuery(FloodDataset.class).asList();
        floods.addAll(floodDatasets);
        // TODO this will need to be updated if there are model based floods

        return floods;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<Flood> searchFloods(String text) {
        Query<FloodDataset> query = this.dataStore.createQuery(FloodDataset.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        List<FloodDataset> floodDatasets = query.asList();

        List<Flood> floods = new ArrayList<>();
        floods.addAll(floodDatasets);

        return floods;
    }
}
