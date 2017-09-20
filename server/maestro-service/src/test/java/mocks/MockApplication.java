package mocks;

import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class MockApplication extends ResourceConfig {
    private static final Logger log = Logger.getLogger(MockApplication.class);

    public MockApplication(Class klass) {
        IRepository mockRepository = new MockRepository();
        mockRepository.initialize();

        super.register(klass);

        super.register(new AbstractBinder() {
            @Override
            protected void configure() {
                super.bind(mockRepository).to(IRepository.class);
            }
        });
    }
}
