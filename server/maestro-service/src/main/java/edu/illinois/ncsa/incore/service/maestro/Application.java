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

import edu.illinois.ncsa.incore.service.maestro.dao.IRepository;
import edu.illinois.ncsa.incore.service.maestro.dao.MongoDBRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        IRepository mongoRepository = new MongoDBRepository("localhost", "maestrodb", 27017);
        mongoRepository.initialize();

        super.register(new AbstractBinder () {

            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IRepository.class);
            }

        });
    }
}
