package edu.illinois.ncsa.incore.services.fragility;

import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    public Application() {
        DataAccess.initializeDataStore();

        if (DataAccess.useCache) {
            DataAccess.loadFragilities();
        }
    }
}
