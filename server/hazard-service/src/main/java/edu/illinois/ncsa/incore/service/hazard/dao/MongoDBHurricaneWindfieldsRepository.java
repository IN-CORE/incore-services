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
import dev.morphia.query.filters.Filters;
import edu.illinois.ncsa.incore.service.hazard.models.hurricaneWindfields.HurricaneWindfields;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public class MongoDBHurricaneWindfieldsRepository implements IHurricaneWindfieldsRepository {
    private String hostUri;
    private final String databaseName;
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
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()),
            mongoClientURI.getDatabase(),
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(HurricaneWindfields.class);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    @Override
    public HurricaneWindfields addHurricaneWindfields(HurricaneWindfields hurricane) {
        String id = this.dataStore.save(hurricane).getId();
        return getHurricaneWindfieldsById(id);
    }

    @Override
    public HurricaneWindfields deleteHurricaneWindfieldsById(String id) {
        HurricaneWindfields hurricane = this.dataStore.find(HurricaneWindfields.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first();
        if (hurricane != null) {
            Query<HurricaneWindfields> query = this.dataStore.find(HurricaneWindfields.class);
            return query.filter(Filters.eq("_id", new ObjectId(id))).findAndDelete();
        }
        return null;
    }

    @Override
    public HurricaneWindfields getHurricaneWindfieldsById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        return this.dataStore.find(HurricaneWindfields.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public List<HurricaneWindfields> getHurricaneWindfields() {
        List<HurricaneWindfields> hurricaneWindfields = this.dataStore.find(HurricaneWindfields.class)
            .iterator().toList();
        return hurricaneWindfields;

    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<HurricaneWindfields> queryHurricaneWindfields(String attributeType, String attributeValue) {
        List<HurricaneWindfields> hurricanes = this.dataStore.find(HurricaneWindfields.class)
            .filter(Filters.eq(attributeType, attributeValue))
            .iterator().toList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> queryHurricaneWindfields(Map<String, String> queryMap) {
        Query<HurricaneWindfields> query = this.dataStore.find(HurricaneWindfields.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query = query.filter(Filters.eq(queryEntry.getKey(), queryEntry.getValue()));
        }

        List<HurricaneWindfields> hurricanes = query.iterator().toList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> searchHurricaneWindfields(String text) {
        Query<HurricaneWindfields> query = this.dataStore.find(HurricaneWindfields.class);
        // need to set text field for name and description
        List<HurricaneWindfields> hurricanes = query.filter(
                Filters.or(
                    Filters.regex("name").pattern(text).caseInsensitive(),
                    Filters.regex("description").pattern(text).caseInsensitive()
                )
            )
            .iterator().toList();

        return hurricanes;
    }

    @Override
    public List<HurricaneWindfields> getHurricaneWindfieldsByCreator(String creator) {
        Query<HurricaneWindfields> query = this.dataStore.find(HurricaneWindfields.class);
        // need to set text field for name and description
        List<HurricaneWindfields> hurricanes = query.filter(
            Filters.regex("creator").pattern(creator).caseInsensitive()
        ).iterator().toList();

        return hurricanes;
    }

    @Override
    public int getHurricaneWindfieldsCountByCreator(String creator) {
        int count = (int) (this.dataStore.find(HurricaneWindfields.class).filter(Filters.regex("creator").
            pattern(creator).caseInsensitive()).count());

        return count;
    }
}
