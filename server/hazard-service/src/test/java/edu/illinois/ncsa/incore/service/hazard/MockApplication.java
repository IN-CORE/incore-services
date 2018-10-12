/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.hazard;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.MockAuthorizer;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.ITsunamiRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MockTornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MockTsunamiRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class clazz) {
        ITsunamiRepository mockTsunamiRepository = new MockTsunamiRepository();
        mockTsunamiRepository.initialize();

        ITornadoRepository mockTornadoRepository = new MockTornadoRepository();
        mockTornadoRepository.initialize();

        IAuthorizer authorizer = new MockAuthorizer(true, true);

        super.register(clazz);
        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(mockTsunamiRepository).to(ITsunamiRepository.class);
                super.bind(mockTornadoRepository).to(ITornadoRepository.class);
                super.bind(authorizer).to(IAuthorizer.class);
            }
        });
    }
}
