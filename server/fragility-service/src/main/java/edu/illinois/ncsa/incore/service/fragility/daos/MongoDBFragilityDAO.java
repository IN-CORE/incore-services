/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.daos;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.fragility.models.FragilitySet;
import edu.illinois.ncsa.incore.service.fragility.typeconverters.BigDecimalConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MongoDBFragilityDAO implements IFragilityDAO {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;
    private List<FragilitySet> fragilities;

    public MongoDBFragilityDAO() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "fragilitydb";
    }

    public MongoDBFragilityDAO(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBFragilityDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
        this.loadFragilities();
    }

    @Override
    public List<FragilitySet> getFragilities() {
        if (this.fragilities == null) {
            this.loadFragilities();
        }

        return this.fragilities;
    }

    @Override
    public FragilitySet saveFragility(FragilitySet fragilitySet) {
        if (fragilitySet == null){
            return null;
        } else {
            this.dataStore.save(fragilitySet);
    
            return fragilitySet;
        }
        
    }

    @Override
    public FragilitySet getById(String id) {
        FragilitySet fragilitySet = this.dataStore.get(FragilitySet.class, id);

        return fragilitySet;
    }

    @Override
    public List<FragilitySet> searchFragilities(String text) {
        Query<FragilitySet> query = this.dataStore.createQuery(FragilitySet.class);

        query.or(query.criteria("demandType").containsIgnoreCase(text),
                 query.criteria("legacyId").containsIgnoreCase(text),
                 query.criteria("hazardType").containsIgnoreCase(text),
                 query.criteria("inventoryType").containsIgnoreCase(text),
                 query.criteria("authors").containsIgnoreCase(text));

        List<FragilitySet> sets = query.limit(100).asList();

        return sets;
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    @Override
    public List<FragilitySet> queryFragilities(String attributeType, String attributeValue) {
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class)
                                                .filter(attributeType, attributeValue)
                                                .limit(100)
                                                .asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilities(Map<String, String> queryMap, int offset, int limit) {
        Query<FragilitySet> query = this.dataStore.createQuery(FragilitySet.class);

        for (Map.Entry<String, String> queryEntry : queryMap.entrySet()) {
            query.filter(queryEntry.getKey(), queryEntry.getValue());
        }

        List<FragilitySet> sets = query.offset(offset).limit(limit).asList();

        return sets;
    }

    @Override
    public List<FragilitySet> queryFragilityAuthor(String author) {
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class)
                                                .field("authors")
                                                .contains(author)
                                                .asList();

        return sets;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);

        Set<Class> classesToMap = new HashSet<>();
        classesToMap.add(FragilitySet.class);
        Morphia morphia = new Morphia(classesToMap);
        morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);

        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    private void loadFragilities() {
        List<FragilitySet> sets = this.dataStore.createQuery(FragilitySet.class).asList();
        fragilities = sets;
    }
}
