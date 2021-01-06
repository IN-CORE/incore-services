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

import com.mongodb.client.MongoClients;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.Query;
import edu.illinois.ncsa.incore.service.data.models.Dataset;
import edu.illinois.ncsa.incore.service.data.models.DatasetType;
import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.mvz.MvzDataset;
import org.bson.Document;
import org.bson.types.ObjectId;

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
        return this.dataStore.find(Dataset.class).iterator().toList();
    }

    public List<MvzDataset> getAllMvzDatasets() {
        return this.dataStore.find(MvzDataset.class).iterator().toList();
    }

    public Dataset getDatasetById(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }
        return this.dataStore.find(Dataset.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    public List<Dataset> getDatasetByType(String type) {
        Query<Dataset> datasetQuery = this.dataStore.find(Dataset.class).filter(Filters.eq(DATASET_FIELD_TYPE, type));
        return datasetQuery.iterator().toList();
    }

    public List<Dataset> getDatasetByTitle(String title) {
        Query<Dataset> datasetQuery = this.dataStore.find(Dataset.class).filter(Filters.eq(DATASET_FIELD_TITLE, title));
        return datasetQuery.iterator().toList();
    }

    public List<Dataset> getDatasetByTypeAndTitle(String type, String title) {
        Query<Dataset> datasetQuery = this.dataStore.find(Dataset.class).filter(Filters.and(
            Filters.eq(DATASET_FIELD_TYPE, type),
            Filters.eq(DATASET_FIELD_TITLE, title)
        ));
        return datasetQuery.iterator().toList();
    }

    public Dataset getDatasetByFileDescriptorId(String id) {
        if (!ObjectId.isValid(id)) {
            return null;
        }

        Query<Dataset> datasetQuery = this.dataStore.find(Dataset.class)
            .filter(Filters.eq(DATASET_FIELD_FILEDESCRIPTOR_ID, new ObjectId(id)));
        return datasetQuery.first();
    }


    public Dataset addDataset(Dataset dataset) {
        String id = this.dataStore.save(dataset).getId().toString();
        return getDatasetById(id);
    }

    public Dataset deleteDataset(String id) {
        Query<Dataset> query = this.dataStore.find(Dataset.class).filter(Filters.eq("_id", new ObjectId(id)));
        return query.findAndDelete();
    }

    public MvzDataset addMvzDataset(MvzDataset mvzDataset) {
        String id = this.dataStore.save(mvzDataset).getId().toString();
        return getMvzDatasetById(id);
    }

    public List<FileDescriptor> getAllFileDescriptors() {
        List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
        List<Dataset> datasets = getAllDatasets();
        for (Dataset dataset : datasets) {
            List<FileDescriptor> fds = dataset.getFileDescriptors();
            fileDescriptors.addAll(fds);
        }

        return fileDescriptors;
    }

    public MvzDataset getMvzDatasetById(String id) {
        return this.dataStore.find(MvzDataset.class).filter(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        // You can call MongoClients.create() without any parameters to connect to a MongoDB instance running on
        // localhost on port 27017
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.ensureIndexes();
        this.dataStore = morphiaStore;
    }

    public Dataset updateDataset(String datasetId, String propName, String propValue) {
        MongoClient client = new MongoClient(mongoClientURI);
        MongoDatabase mongodb = client.getDatabase(databaseName);
        mongodb.getCollection(DATASET_COLLECTION_NAME)
            .updateOne(eq("_id", new ObjectId(datasetId)),
                new Document("$set", new Document(propName, propValue)));
        return getDatasetById(datasetId);
    }

    @Override
    public List<Dataset> searchDatasets(String text) {
        Query<Dataset> query = this.dataStore.find(Dataset.class).filter(Filters.text(text));
        return query.iterator().toList();
    }

    @Override
    public List<DatasetType> getDatatypes(String spaceName) {
        Query<DatasetType> query = this.dataStore.find(DatasetType.class);

        if(spaceName != null){
            query.filter(Filters.eq("space", spaceName));
        }

        return query.iterator().toList();
    }
}
