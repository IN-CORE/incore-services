package edu.illinois.ncsa.incore.services.fragility.test;

import edu.illinois.ncsa.incore.services.fragility.FragilityMappingController;
import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(FragilityMappingController.class);

    public MockApplication() {
        try {
            URL mappingUrl = this.getClass().getClassLoader().getResource("mappings/buildings.xml");
            FragilityMappingController.matchFilterMap = MatchFilterMap.loadMatchFilterMapFromUrl(mappingUrl);

            if (FragilityMappingController.matchFilterMap == null) {
                log.error("Could not load match filter map");
            }
        } catch (DeserializationException ex) {
            log.error("Could not load match filter map", ex);
        }
    }
}
