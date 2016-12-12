package edu.illinois.ncsa.Utils;

import com.mongodb.MongoClient;
import edu.illinois.ncsa.Fragility.Model.FragilitySet;
import edu.illinois.ncsa.TypeConverters.BigDecimalConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashSet;
import java.util.Set;

public class ClientUtils {
    private static final String databaseName = "fragilitydb";
    private static final String collectionName = "fragilitysets";
    private static final String hostUri = "141.142.208.203";
    private static final int port = 27017;

    public static Datastore getDataStore() {
        MongoClient client = new MongoClient(hostUri, port);

        Set<Class> classesToMap = new HashSet<>();
        classesToMap.add(FragilitySet.class);
        Morphia morphia = new Morphia(classesToMap);
        morphia.getMapper().getConverters().addConverter(BigDecimalConverter.class);

        Datastore datastore = morphia.createDatastore(client, databaseName);
        datastore.ensureIndexes();

        return datastore;
    }
}
