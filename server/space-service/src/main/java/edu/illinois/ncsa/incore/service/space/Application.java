/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.space;

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
        String mongodbSpaceUri = "mongodb://localhost:27017/spacedb";

        String mongodbSpaceUriProp = System.getenv("SPACE_MONGODB_URI");
        if (mongodbSpaceUriProp != null && !mongodbSpaceUriProp.isEmpty()) {
            mongodbSpaceUri = mongodbSpaceUriProp;
        }

        ISpaceRepository mongoSpaceRepository = new MongoSpaceDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoSpaceRepository.initialize();

        IUserAllocationsRepository mongoUserAllocationsRepository = new MongoUserAllocationsDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoUserAllocationsRepository.initialize();

        IUserFinalQuotaRepository mongoUserFinalQuotaRepository = new MongoUserFinalQuotaDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoUserFinalQuotaRepository.initialize();

        IUserGroupsRepository mongoUserGroupsRepository = new MongoUserGroupsDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoUserGroupsRepository.initialize();

        IGroupAllocationsRepository mongoGroupAllocationsRepository = new MongoGroupAllocationsDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoGroupAllocationsRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(mongoSpaceRepository).to(ISpaceRepository.class);
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
