/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.project;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.*;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbProjectUri = "mongodb://localhost:27017/projectdb";

        String mongodbProjectUriProp = System.getenv("PROJECT_MONGODB_URI");
        if (mongodbProjectUriProp != null && !mongodbProjectUriProp.isEmpty()) {
            mongodbProjectUri = mongodbProjectUriProp;
        }

        IProjectRepository mongoProjectRepository = new MongoProjectDBRepository(new MongoClientURI(mongodbProjectUri));
        mongoProjectRepository.initialize();

        IUserAllocationsRepository mongoUserAllocationsRepository = new MongoUserAllocationsDBRepository(new MongoClientURI(mongodbProjectUri));
        mongoUserAllocationsRepository.initialize();

        IUserFinalQuotaRepository mongoUserFinalQuotaRepository = new MongoUserFinalQuotaDBRepository(new MongoClientURI(mongodbProjectUri));
        mongoUserFinalQuotaRepository.initialize();

        IUserGroupsRepository mongoUserGroupsRepository = new MongoUserGroupsDBRepository(new MongoClientURI(mongodbProjectUri));
        mongoUserGroupsRepository.initialize();

        IGroupAllocationsRepository mongoGroupAllocationsRepository = new MongoGroupAllocationsDBRepository(new MongoClientURI(mongodbProjectUri));
        mongoGroupAllocationsRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(mongoProjectRepository).to(IProjectRepository.class);
                super.bind(mongoUserAllocationsRepository).to(IUserAllocationsRepository.class);
                super.bind(mongoUserFinalQuotaRepository).to(IUserFinalQuotaRepository.class);
                super.bind(mongoUserGroupsRepository).to(IUserGroupsRepository.class);
                super.bind(mongoGroupAllocationsRepository).to(IGroupAllocationsRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }

        });
        super.register(new CorsFilter());
    }
}
