package mocks;
/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * I (NCSA) - initial API and implementation
 *******************************************************************************/

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.MockAuthorizer;
import edu.illinois.ncsa.incore.common.dao.ISpaceRepository;
import edu.illinois.ncsa.incore.service.project.dao.IProjectRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class klass) {
        IProjectRepository mockRepository = new MockRepository();
        mockRepository.initialize();


        IAuthorizer mockAuthorizer = new MockAuthorizer(true, true);


        super.register(klass);

        super.register(MultiPart.class);
        super.register(MultiPartFeature.class);

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mockRepository).to(IProjectRepository.class);
                super.bind(mockAuthorizer).to(IAuthorizer.class);
            }
        });
    }
}
