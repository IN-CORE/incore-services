package edu.illinois.ncsa.incore.services.maestro;

import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.dataaccess.MongoDBRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        IRepository mongoRepository = new MongoDBRepository("localhost", "maestrodb", 27017);
        mongoRepository.initialize();

        super.register(new AbstractBinder () {

            @Override
            protected void configure() {
                super.bind(mongoRepository).to(IRepository.class);
            }

        });
    }
}
