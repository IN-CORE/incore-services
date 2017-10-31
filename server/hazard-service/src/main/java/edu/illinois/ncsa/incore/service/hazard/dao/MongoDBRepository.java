/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.hazard.models.eq.ScenarioEarthquake;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDBRepository implements IRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;
    private List<ScenarioEarthquake> scenarioEarthquakes;

    // TODO I believe we can just use the URI and remove this later
    public MongoDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBRepository(MongoClientURI mongoClientURI) {
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
        classesToMap.add(ScenarioEarthquake.class);
        Datastore morphiaStore = morphia.createDatastore(client, mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public ScenarioEarthquake getScenarioEarthquakeById(String id) {
        return this.dataStore.get(ScenarioEarthquake.class, new ObjectId(id));
    }

    @Override
    public ScenarioEarthquake addScenarioEarthquake(ScenarioEarthquake scenarioEarthquake) {
        String id = this.dataStore.save(scenarioEarthquake).getId().toString();
        return getScenarioEarthquakeById(id);
    }

    @Override
    public List<ScenarioEarthquake> getScenarioEarthquakes() {
        if (this.scenarioEarthquakes == null) {
            loadScenarioEarthquakes();
        }
        return this.scenarioEarthquakes;
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void loadScenarioEarthquakes() {
        this.scenarioEarthquakes = this.dataStore.createQuery(ScenarioEarthquake.class).asList();
    }
}
