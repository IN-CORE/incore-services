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

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.MongoDBFragilityDAO;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongoHost = "localhost";
        int mongoPort = 27017;

        String mongoHostProp = Config.getConfigProperties().getProperty("fragility.mongodb.host");
        if(mongoHostProp != null && !mongoHostProp.isEmpty()) {
            mongoHost = mongoHostProp;
        }

        String mongoPortProp = Config.getConfigProperties().getProperty("fragility.mongodb.port");
        if(mongoPortProp != null && !mongoPortProp.isEmpty()) {
            try {
                mongoPort = Integer.parseInt(mongoPortProp);
            } catch(NumberFormatException nfe) {
                log.warn("Error parsing fragility.mongodb.port value.", nfe);
            }
        }

        IFragilityDAO mongoRepository = new MongoDBFragilityDAO(mongoHost, "fragilitydb", mongoPort);
        mongoRepository.initialize();

        MatchFilterMap loadedMappings = null;

        try {
            URL mappingUrl = this.getClass().getClassLoader().getResource("mappings/buildings.xml");
            loadedMappings = MatchFilterMap.loadMatchFilterMapFromUrl(mappingUrl);
        } catch (DeserializationException ex) {
            log.error("Could not load match filter map", ex);
        }

        MatchFilterMap matchFilterMap = loadedMappings;

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IFragilityDAO.class);
                if (matchFilterMap != null) {
                    super.bind(matchFilterMap).to(MatchFilterMap.class);
                } else {
                    log.error("Could not set null match filter map");
                }
            }
        });

        super.register(new CorsFilter());
    }
}
