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
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeDataset;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MongoDBEarthquakeRepository implements IEarthquakeRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;

    // TODO I believe we can just use the URI and remove this later
    public MongoDBEarthquakeRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBEarthquakeRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBEarthquakeRepository(MongoClientURI mongoClientURI) {
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
    public Earthquake addEarthquake(Earthquake earthquake) {
        String id = this.dataStore.save(earthquake).getId().toString();
        return getEarthquakeById(id);
    }

    @Override
    public Earthquake getEarthquakeById(String id) {
        // There doesn't seem to be a way to find by the parent class Earthquake
        // TODO Look into this later to see if there is a better way to handle this
        Earthquake earthquake = this.dataStore.get(EarthquakeModel.class, new ObjectId(id));
        if (earthquake == null) {
            earthquake = this.dataStore.get(EarthquakeDataset.class, new ObjectId(id));
        }
        return earthquake;
    }

    @Override
    public List<Earthquake> getEarthquakes() {
        List<Earthquake> earthquakes = new LinkedList<Earthquake>();
        List<EarthquakeModel> earthquakes1 = this.dataStore.createQuery(EarthquakeModel.class).asList();
        List<EarthquakeDataset> earthquakes2 = this.dataStore.createQuery(EarthquakeDataset.class).asList();

        earthquakes.addAll(earthquakes1);
        earthquakes.addAll(earthquakes2);

        return earthquakes;
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }
}
