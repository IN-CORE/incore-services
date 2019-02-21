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

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.MockAuthorizer;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.ITsunamiRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MockTornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.MockTsunamiRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class clazz) {
        ITsunamiRepository mockTsunamiRepository = new MockTsunamiRepository();
        mockTsunamiRepository.initialize();

        ITornadoRepository mockTornadoRepository = new MockTornadoRepository();
        mockTornadoRepository.initialize();

        IAuthorizer authorizer = new MockAuthorizer(true, true);

        super.register(MultiPart.class);
        super.register(MultiPartFeature.class);
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
