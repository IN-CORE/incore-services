package edu.illinois.ncsa.incore.services.maestro.dataaccess;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
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
    private List<Analysis> analyses;

    public MongoDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "maestrodb";
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

    @Override
    public List<Analysis> getAllAnalyses(){
        if(this.analyses == null) {
            this.loadServices();
        }
        return this.analyses;
    }

    @Override
    public Analysis getAnalysisById(String id) {
        return this.dataStore.get(Analysis.class, new ObjectId(id));
    }

    @Override
    public String addAnalysis(Analysis analysis) {
         return this.dataStore.save(analysis).toString();
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

        Set<Class> classesToMap = new HashSet<>();
        Morphia morphia = new Morphia(classesToMap);
        classesToMap.add(Analysis.class);
        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    private void loadServices() {
        List<Analysis> analyses = this.dataStore.createQuery(Analysis.class).asList();
        this.analyses = analyses;
    }

}
