package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.fragility.dataaccess.MongoDBRepository;
import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(FragilityMappingController.class);

    public Application() {
        IRepository mongoRepository = new MongoDBRepository("localhost", "fragilitydb", 27017);
        mongoRepository.initialize();

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IRepository.class);
            }
        });

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
