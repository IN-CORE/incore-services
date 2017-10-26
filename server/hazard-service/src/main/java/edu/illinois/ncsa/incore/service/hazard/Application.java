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

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.service.hazard.dao.IRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MongoDBRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.AtkinsonBoore1995;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class Application  extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        String mongoHost = "localhost";
        int mongoPort = 27017;

        String mongoHostProp = Config.getConfigProperties().getProperty("hazard.mongodb.host");
        if(mongoHostProp != null && !mongoHostProp.isEmpty()) {
            mongoHost = mongoHostProp;
        }

        String mongoPortProp = Config.getConfigProperties().getProperty("hazard.mongodb.port");
        if(mongoPortProp != null && !mongoPortProp.isEmpty()) {
            try {
                mongoPort = Integer.parseInt(mongoPortProp);
            } catch(NumberFormatException nfe) {
                log.warn("Error parsing hazard.mongodb.port value.", nfe);
            }
        }

        IRepository mongoRepository = new MongoDBRepository(mongoHost, "hazarddb", mongoPort);
        mongoRepository.initialize();

        // Bind Atkinson and Boore 1995 model
        // TODO We need some kind of provider where we can register the hazard models

        String modelId = "AtkinsonBoore1995";
        String fileName = modelId + ".csv";
        URL coefficientURL = this.getClass().getClassLoader().getResource("/hazard/earthquake/coefficients/" + fileName);

        AtkinsonBoore1995 model = new AtkinsonBoore1995();
        model.readCoeffients(coefficientURL);

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(model).to(AtkinsonBoore1995.class);
                super.bind(mongoRepository).to(IRepository.class);
            }
        });
    }
}
