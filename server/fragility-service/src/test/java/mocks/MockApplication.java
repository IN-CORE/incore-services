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
import edu.illinois.ncsa.incore.service.fragility.models.mapping.MatchFilterMap;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class... classes) {
        IFragilityDAO mockRepository = new MockFragilityDAO();
        mockRepository.initialize();

        for (Class klass : classes) {
            super.register(klass);
        }

        MatchFilterMap loadedMappings = null;

        try {
            URL mappingUrl = this.getClass().getClassLoader().getResource("mappings/buildings.xml");
            loadedMappings = MatchFilterMap.loadMatchFilterMapFromUrl(mappingUrl);
        } catch (DeserializationException ex) {
            log.error("Could not load match filter map", ex);
        }

        MatchFilterMap matchFilterMap = loadedMappings;

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mockRepository).to(IFragilityDAO.class);
                if (matchFilterMap != null) {
                    super.bind(matchFilterMap).to(MatchFilterMap.class);
                } else {
                    log.error("Could not set null match filter map");
                }
            }
        });
    }
}
