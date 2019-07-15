/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBRepository implements IRepository {
    private final String DATASET_COLLECTION_NAME = "Dataset";
    private final String DATASET_FIELD_TYPE = "dataType";
    private final String DATASET_FIELD_TITLE = "title";
    private final String DATASET_FIELD_FILEDESCRIPTOR_ID = "fileDescriptors._id";
    private String hostUri;
    private String databaseName;
    private int port;
    private MongoClientURI mongoClientURI;
    private Datastore dataStore;

    public MongoDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "datadb";
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

    public List<MvzDataset> getAllMvzDatasets() {
        return this.dataStore.createQuery(MvzDataset.class).asList();
    }

    public Dataset getDatasetById(String id)
    {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        return this.dataStore.get(Dataset.class, new ObjectId(id));
    }

    public List<Dataset> getDatasetByType(String type) {
        Query<Dataset> datasetQuery = this.dataStore.createQuery(Dataset.class);
        datasetQuery.criteria(DATASET_FIELD_TYPE).containsIgnoreCase(type);
        return datasetQuery.asList();
    }

    public List<Dataset> getDatasetByTitle(String title) {
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

    public Dataset getDatasetByFileDescriptorId(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Query<Dataset> datasetQuery = this.dataStore.createQuery(Dataset.class);
        datasetQuery.filter(DATASET_FIELD_FILEDESCRIPTOR_ID, new ObjectId(id));
        return datasetQuery.get();
    }


    public Dataset addDataset(Dataset dataset) {
        String id = this.dataStore.save(dataset).getId().toString();
        return getDatasetById(id);
    }

    public Dataset deleteDataset(String id) {
        Query<Dataset> query = this.dataStore.createQuery(Dataset.class);
        query.field("_id").equal(new ObjectId(id));
        return this.dataStore.findAndDelete(query);
    }

    public MvzDataset addMvzDataset(MvzDataset mvzDataset) {
        String id = this.dataStore.save(mvzDataset).getId().toString();
        return getMvzDatasetById(id);
    }

    public List<FileDescriptor> getAllFileDescriptors(){
        List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
        List<Dataset> datasets = getAllDatasets();
        for (Dataset dataset: datasets) {
            List<FileDescriptor> fds = dataset.getFileDescriptors();
            fileDescriptors.addAll(fds);
        }

        return fileDescriptors;
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
        collection.updateOne(eq("_id", new ObjectId(datasetId)), new Document("$set", new Document(propName, propValue)));
        return getDatasetById(datasetId);
    }

    @Override
    public List<Dataset> searchDatasets(String text) {
        Query<Dataset> query = this.dataStore.createQuery(Dataset.class);

        query.or(query.criteria("title").containsIgnoreCase(text),
            query.criteria("description").containsIgnoreCase(text),
            query.criteria("creator").containsIgnoreCase(text),
            query.criteria("fileDescriptors.filename").containsIgnoreCase(text),
            query.criteria("dataType").containsIgnoreCase(text));

        List<Dataset> datasets = query.asList();

        return datasets;
    }
}
