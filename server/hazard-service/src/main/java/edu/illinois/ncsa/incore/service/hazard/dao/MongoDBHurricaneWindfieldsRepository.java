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
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneDataset;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneWindfields;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MongoDBHurricaneWindfieldsRepository implements IHurricaneWindfieldsRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;

    // TODO I believe we can just use the URI and remove this later
    public MongoDBHurricaneWindfieldsRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBHurricaneWindfieldsRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBHurricaneWindfieldsRepository(MongoClientURI mongoClientURI) {
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
        classesToMap.add(HurricaneWindfields.class);
        Datastore morphiaStore = morphia.createDatastore(client, mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public HurricaneWindfields addHurricaneWindfields(HurricaneWindfields hurricane) {
        String id = this.dataStore.save(hurricane).getId().toString();
        return getHurricaneWindfieldsById(id);
    }

    @Override
    public HurricaneWindfields deleteHurricaneWindfieldsById(String id) {
        HurricaneWindfields hurricane = this.dataStore.createQuery(HurricaneWindfields.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();
        if (hurricane != null) {
            Query<HurricaneWindfields> query = this.dataStore.createQuery(HurricaneWindfields.class);
            query.field("_id").equal(new ObjectId(id));
            return this.dataStore.findAndDelete(query);
        }
        return null;
    }

    @Override
    public HurricaneWindfields getHurricaneWindfieldsById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        return this.dataStore.createQuery(HurricaneWindfields.class)
            .field("_id").equal(new ObjectId(id)).find().tryNext();
    }

    @Override
    public List<HurricaneWindfields> getHurricaneWindfields() {
        List<HurricaneWindfields> hurricaneWindfields = this.dataStore.createQuery(HurricaneWindfields.class).find().toList();
        return hurricaneWindfields;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<HurricaneWindfields> queryHurricaneWindfields(String attributeType, String attributeValue) {
        List<HurricaneWindfields> hurricanes = this.dataStore.createQuery(HurricaneWindfields.class)
            .filter(attributeType, attributeValue)
            .find().toList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> queryHurricaneWindfields(Map<String, String> queryMap) {
        Query<HurricaneWindfields> query = this.dataStore.createQuery(HurricaneWindfields.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<HurricaneWindfields> hurricanes = query.find().toList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> searchHurricaneWindfields(String text) {
        Query<HurricaneWindfields> query = this.dataStore.createQuery(HurricaneWindfields.class);

        query.or(query.criteria("name").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text));

        List<HurricaneWindfields> hurricanes = query.find().toList();

        return hurricanes;
    }
}
