package edu.illinois.ncsa.incore.service.maestro.daos;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;

public abstract class MongoDAO {
    protected MongoClientURI mongoClientURI;
    protected Datastore dataStore;
    protected String databaseName;

    public MongoDAO(MongoClientURI mongoClientURI) {
        this.mongoClientURI = mongoClientURI;
        this.databaseName = mongoClientURI.getDatabase();
    }

    public void initializeDataStore(Class... classes) {
        Datastore morphiaStore = Morphia.createDatastore(MongoClients.create(mongoClientURI.getURI()), databaseName,
            MapperOptions
                .builder()
                .discriminator(DiscriminatorFunction.className())
                .discriminatorKey("className")
                .build()
        );
        morphiaStore.getMapper().map(classes);
        morphiaStore.ensureIndexes();

        this.dataStore = morphiaStore;
    }

    public Datastore getDataStore() {
        return this.dataStore;
    }
}
