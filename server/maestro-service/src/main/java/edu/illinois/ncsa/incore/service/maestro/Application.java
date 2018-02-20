/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.maestro.daos.IRepository;
import edu.illinois.ncsa.incore.service.maestro.daos.MongoDBRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/maestrodb2";

        String mongodbUriProp = Config.getConfigProperties().getProperty("maestro.mongodbURI");
        if(mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }

        IRepository mongoRepository = new MongoDBRepository(new MongoClientURI(mongodbUri));
        mongoRepository.initialize();

        super.register(new AbstractBinder () {

            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IRepository.class);
            }

        });

        super.register(new CorsFilter());
    }
}
