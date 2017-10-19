package edu.illinois.ncsa.incore.services.fragility.dataaccess;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import edu.illinois.ncsa.incore.services.fragility.typeconverters.BigDecimalConverter;
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
    private List<FragilitySet> fragilities;

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
    public Datastore getDataStore() {
        return this.dataStore;
    }

    private void initializeDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

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
