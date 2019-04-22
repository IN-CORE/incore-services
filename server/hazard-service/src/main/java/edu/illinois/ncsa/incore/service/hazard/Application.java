/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

import com.mongodb.MongoClientURI;
import edu.illinois.ncsa.incore.common.auth.Authorizer;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.common.dao.MongoSpaceDBRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.*;
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

        ITsunamiRepository tsunamiRepository = new MongoDBTsunamiRepository(new MongoClientURI(mongodbUri));
        tsunamiRepository.initialize();

        IHurricaneRepository hurricaneRepository = new MongoDBHurricaneRepository(new MongoClientURI(mongodbUri));
        hurricaneRepository.initialize();

        String mongodbSpaceUri = "mongodb://localhost:27017/spacedb";

        String mongodbSpaceUriProp = Config.getConfigProperties().getProperty("space.mongodbURI");
        if(mongodbSpaceUriProp != null && !mongodbSpaceUriProp.isEmpty()) {
            mongodbSpaceUri = mongodbSpaceUriProp;
        }

        ISpaceRepository mongoSpaceRepository = new MongoSpaceDBRepository(new MongoClientURI(mongodbSpaceUri));
        mongoSpaceRepository.initialize();

        IAuthorizer authorizer = Authorizer.getInstance();
        AttenuationProvider attenuationProvider = AttenuationProvider.getInstance();

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(attenuationProvider).to(AttenuationProvider.class);
                super.bind(earthquakeRepository).to(IEarthquakeRepository.class);
                super.bind(tornadoRepository).to(ITornadoRepository.class);
                super.bind(hurricaneRepository).to(IHurricaneRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
                super.bind(tsunamiRepository).to(ITsunamiRepository.class);
                super.bind(mongoSpaceRepository).to(ISpaceRepository.class);
            }
        });
        super.register(new CorsFilter());
    }
}
