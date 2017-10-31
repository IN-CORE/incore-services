/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import edu.illinois.ncsa.incore.service.data.model.mvz.MvzDataset;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBRepository implements IRepository {
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;
    private final String DATASET_COLLECTION_NAME = "Dataset";  //$NON-NLS-1$
    private final String DATASET_FIELD_NAME = "name";   //$NON-NLS-1$
    private final String DATASET_FIELD_TYPE = "type";   //$NON-NLS-1$
    private final String DATASET_FIELD_TITLE = "title"; //$NON-NLS-1$

    private Datastore dataStore;

    public MongoDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost"; //$NON-NLS-1$
        this.databaseName = "datadb";   //$NON-NLS-1$
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

    public List<Dataset> getAllDatasets() {
        return this.dataStore.createQuery(Dataset.class).asList();
    }

    public List<Space> getAllSpaces() {
        return this.dataStore.createQuery(Space.class).asList();
    }

    public Dataset getDatasetById(String id) {
        return this.dataStore.get(Dataset.class, new ObjectId(id));
    }

    public List<Dataset> getDatasetByType(String type){
        Query<Dataset> datasetQuery = this.dataStore.createQuery(Dataset.class);
        datasetQuery.criteria(DATASET_FIELD_TYPE).containsIgnoreCase(type);
        return datasetQuery.asList();
    }

    public List<Dataset> getDatasetByTitle(String title){
        Query<Dataset> datasetQuery = this.dataStore.createQuery(Dataset.class);
        datasetQuery.criteria(DATASET_FIELD_TITLE).containsIgnoreCase(title);
        datasetQuery.getSortObject();
        return datasetQuery.asList();
    }

    public List<Dataset> getDatasetByTypeAndTitle(String type, String title) {
        Query<Dataset> datasetQuery = this.dataStore.createQuery(Dataset.class);
        datasetQuery.and(
                datasetQuery.criteria(DATASET_FIELD_TYPE).containsIgnoreCase(type),
                datasetQuery.criteria(DATASET_FIELD_TITLE).containsIgnoreCase(title)
        );
        return datasetQuery.asList();
    }

    public Dataset addDataset(Dataset dataset) {
        String id = this.dataStore.save(dataset).getId().toString();
        return getDatasetById(id);
    }

    public Space getSpaceById(String id) {
        return this.dataStore.get(Space.class, new ObjectId(id));
    }

    public Space getSpaceByName(String name) {
        Query<Space> spaceQuery = this.dataStore.createQuery(Space.class);
        spaceQuery.field(DATASET_FIELD_NAME).equal(name);
        Space foundSpace = spaceQuery.get();

        return foundSpace;
    }

    public Space addSpace(Space space) {
        String id = this.dataStore.save(space).getId().toString();
        return getSpaceById(id);
    }

    public MvzDataset addMvzDataset(MvzDataset mvzDataset) {
        String id = this.dataStore.save(mvzDataset).getId().toString();
        return getMvzDatasetById(id);
    }

    public MvzDataset getMvzDatasetById(String id) {
        return this.dataStore.get(MvzDataset.class, new ObjectId(id));
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(mongoClientURI);
        Set<Class> classesToMap = new HashSet<>();
        Morphia morphia = new Morphia(classesToMap);
        classesToMap.add(Dataset.class);
        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    public Dataset updateDataset(String datasetId, String propName, String propValue) {
        MongoClient client = new MongoClient(mongoClientURI);
        MongoDatabase mongodb = client.getDatabase(databaseName);
        MongoCollection collection = mongodb.getCollection(DATASET_COLLECTION_NAME);
        collection.updateOne(eq("_id", new ObjectId(datasetId)), new Document("$set", new Document(propName, propValue)));  //$NON-NLS-1$ //$NON-NLS-2$
        return getDatasetById(datasetId);
    }
}
