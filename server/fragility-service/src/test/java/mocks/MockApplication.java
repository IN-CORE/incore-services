/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package mocks;

import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.IMappingDAO;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class... classes) {
        IFragilityDAO fragilityDAO = new MockFragilityDAO();
        fragilityDAO.initialize();

        IMappingDAO mappingDAO = new MockMappingDAO();
        mappingDAO.initialize();

        for (Class klass : classes) {
            super.register(klass);
        }

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(fragilityDAO).to(IFragilityDAO.class);
                super.bind(mappingDAO).to(IMappingDAO.class);
            }
        });
    }
}
