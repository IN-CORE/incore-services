package mocks;

import edu.illinois.ncsa.incore.services.fragility.FragilityController;
import edu.illinois.ncsa.incore.services.fragility.FragilityMappingController;
import edu.illinois.ncsa.incore.services.fragility.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import ncsa.tools.common.exceptions.DeserializationException;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URL;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(FragilityMappingController.class);

    public MockApplication(Class klass) {
        IRepository repository = new MockRepository();
        repository.initialize();

        super.register(klass);
        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(repository).to(IRepository.class);
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
