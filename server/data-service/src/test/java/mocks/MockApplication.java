/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * I (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import edu.illinois.ncsa.incore.service.data.dao.IRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class klass) {
        IRepository mockRepository = new MockRepository();
        mockRepository.initialize();

        super.register(klass);

        super.register(MultiPart.class);
        super.register(MultiPartFeature.class);
//        super.register(MultiPartWriter.class);
//        super.register(MultiPartResource.class);

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mockRepository).to(IRepository.class);
            }
        });
    }
}
