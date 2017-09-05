package edu.illinois.ncsa.incore.services.fragility;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import edu.illinois.ncsa.incore.services.fragility.typeconverters.BigDecimalConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataAccess {
    private static final String databaseName = "fragilitydb";
    private static final String hostUri = "localhost";
    private static final int port = 27017;

    private static Datastore store;
    private static List<FragilitySet> fragilities;

    public static final boolean useCache = true;

    public static void initializeDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

        Set<Class> classesToMap = new HashSet<>();
        classesToMap.add(FragilitySet.class);
        Morphia morphia = new Morphia(classesToMap);
        morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);

        Datastore datastore = morphia.createDatastore(client, databaseName);
        datastore.ensureIndexes();
        store = datastore;
    }

    public static void loadFragilities() {
        List<FragilitySet> sets = getDataStore().createQuery(FragilitySet.class).asList();
        fragilities = sets;
    }

    public static Datastore getDataStore() {
        if (store == null) {
            initializeDataStore();
        }

        return store;
    }

    public static List<FragilitySet> getFragilities() {
        if (fragilities == null) {
            loadFragilities();
        }

        return fragilities;
    }
}
