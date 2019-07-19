/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/

package mocks;

import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.common.auth.MockAuthorizer;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.fragility.daos.IFragilityMappingDAO;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class... classes) {
        IFragilityDAO fragilityDAO = new MockFragilityDAO();
        fragilityDAO.initialize();

        IFragilityMappingDAO mappingDAO = new MockFragilityMappingDAO();
        mappingDAO.initialize();

        for (Class klass : classes) {
            super.register(klass);
        }

        IAuthorizer mockAuthorizor = new MockAuthorizer(true, true);

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(fragilityDAO).to(IFragilityDAO.class);
                super.bind(mappingDAO).to(IFragilityMappingDAO.class);
                super.bind(mockAuthorizor).to(IAuthorizer.class);
            }
        });
    }
}
