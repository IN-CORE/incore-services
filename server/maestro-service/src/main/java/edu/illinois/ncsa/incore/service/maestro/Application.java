/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.service.maestro.daos.IPlaybookDAO;
import edu.illinois.ncsa.incore.service.maestro.daos.MongoDBPlaybookDAO;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/maestrodb2";

        String mongodbUriProp = System.getenv("MAESTRO_MONGODB_URI");
        if (mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }

        IPlaybookDAO mongoRepository = new MongoDBPlaybookDAO(new MongoClientURI(mongodbUri));
        mongoRepository.initialize();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IPlaybookDAO.class);
            }

        });

        super.register(new CorsFilter());
    }
}
