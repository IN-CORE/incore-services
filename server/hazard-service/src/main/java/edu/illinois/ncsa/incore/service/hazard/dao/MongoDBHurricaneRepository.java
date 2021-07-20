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
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneDataset;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(HurricaneDataset.class);
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
        Hurricane hurricane = this.dataStore.find(HurricaneDataset.class)
            .filter(Filters.eq("_id", new ObjectId(id)))
            .first();
        if (hurricane != null) {
            Query<HurricaneDataset> query = this.dataStore.find(HurricaneDataset.class)
                .filter(Filters.eq("_id", new ObjectId(id)));
            return query.findAndDelete();
        }
        return null;
    }

    @Override
    public Hurricane getHurricaneById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Hurricane hurricane = this.dataStore.find(HurricaneDataset.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        // TODO this will need to be updated if there are model based hurricanes

        return hurricane;
    }

    @Override
    public List<Hurricane> getHurricanes() {
        List<Hurricane> hurricanes = new LinkedList<>();
        List<HurricaneDataset> hurricaneDatasets = this.dataStore.find(HurricaneDataset.class)
            .iterator().toList();
        hurricanes.addAll(hurricaneDatasets);
        // TODO this will need to be updated if there are model based hurricanes

        return hurricanes;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<Hurricane> searchHurricanes(String text) {
        Query<HurricaneDataset> query = this.dataStore.find(HurricaneDataset.class);

        List<HurricaneDataset> hurricaneDatasets = query.filter(
            Filters.or(
                Filters.regex("name").pattern(text).caseInsensitive(),
                Filters.regex("description").pattern(text).caseInsensitive()
            )
        )
            .iterator().toList();

        List<Hurricane> hurricanes = new ArrayList<>();
        hurricanes.addAll(hurricaneDatasets);

        return hurricanes;
    }

    @Override
    public List<Hurricane> getHurricanesByCreator(String creator) {
        Query<HurricaneDataset> query = this.dataStore.find(HurricaneDataset.class);

        List<HurricaneDataset> hurricaneDatasets = query.filter(
            Filters.regex("creator").pattern(creator).caseInsensitive()
        ).iterator().toList();

        List<Hurricane> hurricanes = new ArrayList<>();
        hurricanes.addAll(hurricaneDatasets);

        return hurricanes;
    }

    @Override
    public int getHurricanesCountByCreator(String creator) {
        int count = (int) (this.dataStore.find(HurricaneDataset.class).filter(Filters.regex("creator").
            pattern(creator).caseInsensitive()).count());

        return count;
    }
}
