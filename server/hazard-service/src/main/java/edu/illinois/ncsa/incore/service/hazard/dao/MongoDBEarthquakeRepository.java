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
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;

import java.util.*;

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
    public Earthquake deleteEarthquakeById(String id) {
        Earthquake earthquake = this.dataStore.createQuery(EarthquakeModel.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();
        if (earthquake == null) {
            Query<EarthquakeDataset> query = this.dataStore.createQuery(EarthquakeDataset.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        } else {
            Query<EarthquakeModel> query = this.dataStore.createQuery(EarthquakeModel.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        }
    }

    @Override
    public Earthquake getEarthquakeById(String id) {
        // There doesn't seem to be a way to find by the parent class Earthquake
        // TODO Look into this later to see if there is a better way to handle this

        if (!ObjectId.isValid(id)) {
            return null;
        }

        Earthquake earthquake = this.dataStore.createQuery(EarthquakeModel.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();
        if (earthquake == null) {
            earthquake = this.dataStore.createQuery(EarthquakeDataset.class)
                .field("_id").equal(new ObjectId(id)).find().tryNext();
        }
        return earthquake;
    }

    @Override
    public List<Earthquake> getEarthquakes() {
        List<Earthquake> earthquakes = new LinkedList<Earthquake>();
        List<EarthquakeModel> earthquakes1 = this.dataStore.createQuery(EarthquakeModel.class).find().toList();
        List<EarthquakeDataset> earthquakes2 = this.dataStore.createQuery(EarthquakeDataset.class).find().toList();

        earthquakes.addAll(earthquakes1);
        earthquakes.addAll(earthquakes2);

        return earthquakes;
    }

    @Override
    public List<Earthquake> searchEarthquakes(String text) {
        Query<EarthquakeDataset> query = this.dataStore.createQuery(EarthquakeDataset.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        Query<EarthquakeModel> modelQuery = this.dataStore.createQuery(EarthquakeModel.class);

        modelQuery.or(modelQuery.criteria("name").containsIgnoreCase(text),
            modelQuery.criteria("description").containsIgnoreCase(text));

        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakes.addAll(query.find().toList());
        earthquakes.addAll(modelQuery.find().toList());

        return earthquakes;
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }
}
