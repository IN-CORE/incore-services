/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.daos;

import com.mongodb.MongoClientURI;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import edu.illinois.ncsa.incore.service.maestro.models.Playbook;
import org.bson.types.ObjectId;

import java.util.List;

public class MongoDBPlaybookDAO implements IPlaybookDAO {
    private String hostUri;
    private final String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;

    private Datastore dataStore;
    private List<Playbook> playbooks;

    public MongoDBPlaybookDAO() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "maestrodb";
    }

    public MongoDBPlaybookDAO(String hostUri, String databaseName, int port) {
        this.databaseName = databaseName;
        this.hostUri = hostUri;
        this.port = port;
    }

    public MongoDBPlaybookDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    @Override
    public List<Playbook> getAllPlaybooks() {
        this.loadServices();
        return this.playbooks;
    }

    @Override
    public Playbook getPlaybookById(String id) {

        return this.dataStore.get(Analysis.class, new ObjectId(id));
    }

    @Override
    public Analysis addAnalysis(Analysis analysis) {
        String id = this.dataStore.save(analysis).getId();
        return getAnalysisById(id);
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(Analysis.class);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    private void loadServices() {
        List<Analysis> analyses = this.dataStore.find(Analysis.class).toList();
        this.analyses = analyses;
    }

}
