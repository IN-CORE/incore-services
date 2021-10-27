/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package mocks;

import edu.illinois.ncsa.incore.service.maestro.daos.IPlaybookDAO;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class clazz) {
        IPlaybookDAO playbookDAO = new MockRepository();
        playbookDAO.initialize();

        super.register(clazz);


        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(playbookDAO).to(IPlaybookDAO.class);
            }
        });
    }
}
