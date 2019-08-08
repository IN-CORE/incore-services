/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.MongoSpaceDBRepository;
import edu.illinois.ncsa.incore.service.dfr3.daos.*;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/dfr3db";

        String mongodbUriProp = Config.getConfigProperties().getProperty("dfr3.mongodbURI");
        if (mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }

        // use same instance of mongo client
        MongoClientURI mongoClientUri = new MongoClientURI(mongodbUri);
        IFragilityDAO fragilityDAO = new MongoDBFragilityDAO(mongoClientUri);
        fragilityDAO.initialize();

        IMappingDAO mappingDAO = new MongoDBMappingDAO(mongoClientUri);
        mappingDAO.initialize();

        IRestorationDAO restorationDAO = new MongoDBRestorationDAO(mongoClientUri);
        restorationDAO.initialize();

        String mongodbSpaceUri = "mongodb://localhost:27017/spacedb";

        String mongodbSpaceUriProp = Config.getConfigProperties().getProperty("space.mongodbURI");
        if(mongodbSpaceUriProp != null && !mongodbSpaceUriProp.isEmpty()) {
            mongodbSpaceUri = mongodbSpaceUriProp;
        }

        ISpaceRepository mongoSpaceRepository = new MongoSpaceDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoSpaceRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(fragilityDAO).to(IFragilityDAO.class);
                super.bind(mappingDAO).to(IMappingDAO.class);
                super.bind(restorationDAO).to(IRestorationDAO.class);
                super.bind(mongoSpaceRepository).to(ISpaceRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }
        });

        super.register(new CorsFilter());
    }
}
