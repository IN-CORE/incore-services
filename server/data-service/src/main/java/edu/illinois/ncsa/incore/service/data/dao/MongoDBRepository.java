package edu.illinois.ncsa.incore.service.data.dao;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDBRepository implements IRepository {
    private String hostUri;
    private String databaseName;
    private int port;

    private Datastore dataStore;
    private List<Dataset> datasets;

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

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Dataset> getAllDatasets(){
        if(this.datasets == null) {
            this.loadServices();
        }
        return this.datasets;
    }

    public Dataset getDatasetById(String id) {
        return this.dataStore.get(Dataset.class, new ObjectId(id));
    }

    public Dataset addDataset(Dataset dataset) {
         String id = this.dataStore.save(dataset).getId().toString();
         return getDatasetById(id);
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

        Set<Class> classesToMap = new HashSet<>();
        Morphia morphia = new Morphia(classesToMap);
        classesToMap.add(Dataset.class);
        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    private void loadServices() {
        List<Dataset> datasets = this.dataStore.createQuery(Dataset.class).asList();
        this.datasets = datasets;
    }

}
