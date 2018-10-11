/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.dao.IEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MongoDBEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MongoDBTornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.AttenuationProvider;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongodbUri = "mongodb://localhost:27017/hazarddb";

        String mongodbUriProp = Config.getConfigProperties().getProperty("hazard.mongodbURI");
        if (mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }

        IEarthquakeRepository earthquakeRepository = new MongoDBEarthquakeRepository(new MongoClientURI(mongodbUri));
        earthquakeRepository.initialize();

        ITornadoRepository tornadoRepository = new MongoDBTornadoRepository(new MongoClientURI(mongodbUri));
        tornadoRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();
        AttenuationProvider attenuationProvider = AttenuationProvider.getInstance();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(attenuationProvider).to(AttenuationProvider.class);
                super.bind(earthquakeRepository).to(IEarthquakeRepository.class);
                super.bind(tornadoRepository).to(ITornadoRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }
        });
        super.register(new CorsFilter());
    }
}
