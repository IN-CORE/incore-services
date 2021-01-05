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

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.TornadoDataset;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.TornadoModel;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;

import java.util.*;

public class MongoDBTornadoRepository implements ITornadoRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;

    public MongoDBTornadoRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "hazarddb";
    }

    public MongoDBTornadoRepository(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBTornadoRepository(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    private void initializeDataStore() {

//        Set<Class> classesToMap = new HashSet<>();
//        classesToMap.add(Tornado.class);
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(), mongoClientURI.getDatabase());
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public List<Tornado> getTornadoes() {
        List<Tornado> tornadoes = new LinkedList<Tornado>();
        tornadoes.addAll(this.dataStore.find(TornadoModel.class).iterator().toList());
        tornadoes.addAll(this.dataStore.find(TornadoDataset.class).iterator().toList());

        return tornadoes;
    }

    @Override
    public List<Tornado> searchTornadoes(String text) {
        Query<TornadoDataset> datasetQuery = this.dataStore.find(TornadoDataset.class).filter(Filters.text(text));
        Query<TornadoModel> modelQuery = this.dataStore.find(TornadoModel.class).filter(Filters.text(text));

        List<TornadoDataset> tornadoDatasets = datasetQuery.iterator().toList();
        List<TornadoModel> tornadoModels = modelQuery.iterator().toList();

        List<Tornado> tornadoes = new ArrayList<>();
        tornadoes.addAll(tornadoDatasets);
        tornadoes.addAll(tornadoModels);

        return tornadoes;
    }

    @Override
    public Tornado addTornado(Tornado tornado) {
        String id = this.dataStore.save(tornado).getId().toString();
        return getTornadoById(id);
    }

    @Override
    public Tornado deleteTornadoById(String id) {
        Tornado tornado = this.dataStore.find(TornadoModel.class).filter(Filters.eq("_id", new ObjectId(id)))
            .first();
        if (tornado == null) {
            Query<TornadoDataset> query = this.dataStore.find(TornadoDataset.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        } else {
            Query<TornadoModel> query = this.dataStore.find(TornadoModel.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
    }

    @Override
    public Tornado getTornadoById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Tornado tornado = this.dataStore.find(TornadoModel.class).filter(Filters.eq("_id", new ObjectId(id)))
            .first();
        if (tornado != null) {
            return tornado;
        }
        return this.dataStore.find(TornadoDataset.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }
}
