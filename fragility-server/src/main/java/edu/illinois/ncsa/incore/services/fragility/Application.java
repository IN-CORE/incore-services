package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(FragilityMappingController.class);

    public Application() {
        DataAccess.initializeDataStore();

        if (DataAccess.useCache) {
            DataAccess.loadFragilities();
        }

        try {
            URL mappingUrl = this.getClass().getClassLoader().getResource("mappings/buildings.xml");
            MatchFilterMap matchFilterMap = MatchFilterMap.loadMatchFilterMapFromUrl(mappingUrl);
            FragilityMappingController.matchFilterMap = matchFilterMap;
            FragilityController.matchFilterMap = matchFilterMap;

            if (FragilityMappingController.matchFilterMap == null) {
                log.error("Could not load match filter map");
            }
        } catch (DeserializationException ex) {
            log.error("Could not load match filter map", ex);
        }
    }
}
