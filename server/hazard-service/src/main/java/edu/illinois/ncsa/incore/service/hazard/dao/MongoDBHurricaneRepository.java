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
import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeDataset;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeModel;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.ScenarioHurricane;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    public ScenarioHurricane addHurricane(ScenarioHurricane hurricane) {
        String id = this.dataStore.save(hurricane).getId().toString();
        return getHurricaneById(id);
    }

    @Override
    public ScenarioHurricane getHurricaneById(String id) {
        return this.dataStore.get(ScenarioHurricane.class, new ObjectId(id));
    }

    @Override
    public List<ScenarioHurricane> getHurricanes() {
        List<ScenarioHurricane> scenarioHurricanes = this.dataStore.createQuery(ScenarioHurricane.class).asList();
        return scenarioHurricanes;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }
}
