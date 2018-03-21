
package edu.illinois.ncsa.incore.service.semantic;

import edu.illinois.ncsa.incore.semantic.units.instances.Units;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    public Application() {
        Units.initialize();
    }
}
