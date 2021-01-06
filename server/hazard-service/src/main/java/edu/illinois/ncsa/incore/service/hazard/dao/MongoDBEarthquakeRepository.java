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
import com.mongodb.client.MongoClients;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.experimental.filters.Filters;
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
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Earthquake.class);
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
        Earthquake earthquake = this.dataStore.find(EarthquakeModel.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        if (earthquake == null) {
            Query<EarthquakeDataset> query = this.dataStore.find(EarthquakeDataset.class);
            return query.filter(Filters.eq("_id", new ObjectId(id))).findAndDelete();
        } else {
            Query<EarthquakeModel> query = this.dataStore.find(EarthquakeModel.class);
            return query.filter(Filters.eq("_id", new ObjectId(id))).findAndDelete();
        }
    }

    @Override
    public Earthquake getEarthquakeById(String id) {
        // There doesn't seem to be a way to find by the parent class Earthquake
        // TODO Look into this later to see if there is a better way to handle this

        if (!ObjectId.isValid(id)) {
            return null;
        }

        Earthquake earthquake = this.dataStore.find(EarthquakeModel.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        if (earthquake == null) {
            earthquake = this.dataStore.find(EarthquakeDataset.class)
                .filter(Filters.eq("_id", new ObjectId(id))).first();
        }
        return earthquake;
    }

    @Override
    public List<Earthquake> getEarthquakes() {
        List<Earthquake> earthquakes = new LinkedList<Earthquake>();
        List<EarthquakeModel> earthquakes1 = this.dataStore.find(EarthquakeModel.class).iterator().toList();
        List<EarthquakeDataset> earthquakes2 = this.dataStore.find(EarthquakeDataset.class).iterator().toList();

        earthquakes.addAll(earthquakes1);
        earthquakes.addAll(earthquakes2);

        return earthquakes;
    }

    @Override
    public List<Earthquake> searchEarthquakes(String text) {
        Query<EarthquakeDataset> query = this.dataStore.find(EarthquakeDataset.class)
            .filter(Filters.text(text).caseSensitive(false));

        Query<EarthquakeModel> modelQuery = this.dataStore.find(EarthquakeModel.class)
            .filter(Filters.text(text).caseSensitive(false));

        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakes.addAll(query.iterator().toList());
        earthquakes.addAll(modelQuery.iterator().toList());

        return earthquakes;
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }
}
