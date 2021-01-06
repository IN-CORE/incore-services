/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.dao;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
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
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(FloodDataset.class);
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
        Flood flood = this.dataStore.find(FloodDataset.class).filter(Filters.eq("_id", new ObjectId(id))).first();
        if (flood != null) {
            Query<FloodDataset> query = this.dataStore.find(FloodDataset.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
        return null;
    }

    @Override
    public Flood getFloodById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Flood flood = this.dataStore.find(FloodDataset.class).filter(Filters.eq("_id", new ObjectId(id))).first();
        // TODO this will need to be updated if there are model based floods

        return flood;
    }

    @Override
    public List<Flood> getFloods() {
        List<Flood> floods = new LinkedList<>();
        List<FloodDataset> floodDatasets = this.dataStore.find(FloodDataset.class).iterator().toList();
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
        Query<FloodDataset> query = this.dataStore.find(FloodDataset.class);

        List<FloodDataset> floodDatasets = query.filter(Filters.text(text).caseSensitive(false)).iterator().toList();
        List<Flood> floods = new ArrayList<>();
        floods.addAll(floodDatasets);

        return floods;
    }
}
