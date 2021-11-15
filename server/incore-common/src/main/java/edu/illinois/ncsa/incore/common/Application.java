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

package edu.illinois.ncsa.incore.common;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.MongoSpaceDBRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbCommonUri = "mongodb://localhost:27017/commondb";

        String mongodbCommonUriProp = System.getenv("COMMON_MONGODB_URI");
        if (mongodbCommonUriProp != null && !mongodbCommonUriProp.isEmpty()) {
            mongodbCommonUri = mongodbCommonUriProp;
        }

        ISpaceRepository mongoCommonRepository = new MongoSpaceDBRepository(new MongoClientURI(mongodbCommonUri));
        mongoCommonRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(mongoCommonRepository).to(ISpaceRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }

        });
//        super.register(new CorsFilter());
    }
}
