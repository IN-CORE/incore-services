/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.MongoDBFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.MongoDBMappingDAO;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/fragilitydb";

        String mongodbUriProp = Config.getConfigProperties().getProperty("fragility.mongodbURI");
        if (mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }

        // use same instance of mongo client
        MongoClientURI mongoClientUri = new MongoClientURI(mongodbUri);
        IFragilityDAO fragilityDAO = new MongoDBFragilityDAO(mongoClientUri);
        fragilityDAO.initialize();
        IMappingDAO mappingDAO = new MongoDBMappingDAO(mongoClientUri);
        mappingDAO.initialize();

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(fragilityDAO).to(IFragilityDAO.class);
                super.bind(mappingDAO).to(IMappingDAO.class);
            }
        });

        super.register(new CorsFilter());
    }
}
