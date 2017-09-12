package edu.illinois.ncsa.incore.services.maestro.dataaccess;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.services.maestro.model.Service;
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
    private List<Service> services;

    public MongoDBRepository() {
        this.port = 27017;
        this.hostUri = "localhost";
        this.databaseName = "fragilitydb";
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
    public List<Service> getAllServices(){
        if(this.services == null) {
            this.loadServices();
        }
        return this.services;
    }

    @Override
    public Service getServiceById(String id) {
        return this.dataStore.get(Service.class, id);
    }

    @Override
    public String addService(Service service) {
         return this.dataStore.save(service).toString();
    }

    @Override
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

        Set<Class> classesToMap = new HashSet<>();
        Morphia morphia = new Morphia(classesToMap);
        classesToMap.add(Service.class);
        Datastore morphiaStore = morphia.createDatastore(client, databaseName);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    private void loadServices() {
        List<Service> services = this.dataStore.createQuery(Service.class).asList();
        this.services = services;
    }



}
