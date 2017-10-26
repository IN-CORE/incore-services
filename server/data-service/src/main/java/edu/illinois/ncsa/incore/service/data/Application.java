/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import edu.illinois.ncsa.incore.service.data.dao.MongoDBRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.HashSet;
import java.util.Set;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongoHost = "localhost";
        int mongoPort = 27017;

        String mongoHostProp = Config.getConfigProperties().getProperty("data.mongodb.host");
        if(mongoHostProp != null && !mongoHostProp.isEmpty()) {
            mongoHost = mongoHostProp;
        }

        String mongoPortProp = Config.getConfigProperties().getProperty("data.mongodb.port");
        if(mongoPortProp != null && !mongoPortProp.isEmpty()) {
            try {
                mongoPort = Integer.parseInt(mongoPortProp);
            } catch(NumberFormatException nfe) {
                log.warn("Error parsing data.mongodb.port value.", nfe);
            }
        }

        IRepository mongoRepository = new MongoDBRepository(mongoHost, "datadb", mongoPort);
        mongoRepository.initialize();

        super.register(new AbstractBinder () {

            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IRepository.class);
            }

        });
    }
}
