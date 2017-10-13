package edu.illinois.ncsa.incore.service.data.dao;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.service.data.model.Space;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.Dataset;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDBRepository implements IRepository {
    private String hostUri;
    private String databaseName;
    private int port;

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

    @Override
    public void initialize() {
        this.initializeDataStore();
    }

    public List<Dataset> getAllDatasets(){
        return this.dataStore.createQuery(Dataset.class).asList();
    }

    public List<Space> getAllSpaces() {
        return this.dataStore.createQuery(Space.class).asList();
    }

    public Dataset getDatasetById(String id) {
        return this.dataStore.get(Dataset.class, new ObjectId(id));
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
        spaceQuery.field("name").equal(name);
        Space foundSpace = spaceQuery.get();

        return foundSpace;
    }

    public Space addSpace(Space space) {
        String id = this.dataStore.save(space).getId().toString();
        return getSpaceById(id);
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
}
