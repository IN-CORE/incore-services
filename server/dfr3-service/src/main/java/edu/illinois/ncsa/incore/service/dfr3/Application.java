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
import edu.illinois.ncsa.incore.common.dao.ICommonRepository;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.MongoCommonDBRepository;
import edu.illinois.ncsa.incore.common.dao.MongoSpaceDBRepository;
import edu.illinois.ncsa.incore.service.dfr3.daos.*;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/dfr3db";

        String mongodbUriProp = System.getenv("DFR3_MONGODB_URI");
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

        IRepairDAO repairDAO = new MongoDBRepairDAO(mongoClientUri);
        repairDAO.initialize();

        String mongodbSpaceUri = "mongodb://localhost:27017/spacedb";

        String mongodbSpaceUriProp = System.getenv("SPACE_MONGODB_URI");
        if (mongodbSpaceUriProp != null && !mongodbSpaceUriProp.isEmpty()) {
            mongodbSpaceUri = mongodbSpaceUriProp;
        }

        ISpaceRepository mongoSpaceRepository = new MongoSpaceDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoSpaceRepository.initialize();

        // connect to config database to get definitions
        String mongodbCommonUri = "mongodb://localhost:27017/commondb";
        String mongodbCommonUriProp = System.getenv("COMMON_MONGODB_URI");
        if (mongodbCommonUriProp != null && !mongodbCommonUriProp.isEmpty()) {
            mongodbCommonUri = mongodbCommonUriProp;
        }

        ICommonRepository mongoCommonRepository = new MongoCommonDBRepository(new MongoClientURI(mongodbCommonUri));
        mongoCommonRepository.initialize();


        IAuthorizer authorizer = Authorizer.getInstance();

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(fragilityDAO).to(IFragilityDAO.class);
                super.bind(mappingDAO).to(IMappingDAO.class);
                super.bind(restorationDAO).to(IRestorationDAO.class);
                super.bind(repairDAO).to(IRepairDAO.class);
                super.bind(mongoSpaceRepository).to(ISpaceRepository.class);
                super.bind(mongoCommonRepository).to(ICommonRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }
        });

        super.register(new CorsFilter());
    }
}
