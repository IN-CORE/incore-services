package edu.illinois.ncsa.incore.services.hazard;

import edu.illinois.ncsa.incore.services.hazard.models.eq.attenuations.AtkinsonBoore1995;
import org.apache.log4j.Logger;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.net.URL;

public class Application  extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        // Bind Atkinson and Boore 1995 model
        // TODO We need some kind of provider where we can register the hazard models

        String modelId = "AtkinsonBoore1995";
        String fileName = modelId + ".csv";
        URL coefficientURL = this.getClass().getClassLoader().getResource("/hazard/earthquake/coefficients/" + fileName);

        AtkinsonBoore1995 model = new AtkinsonBoore1995();
        model.readCoeffients(coefficientURL);

        super.register(new AbstractBinder() {

            @Override
            protected void configure() {
                super.bind(model).to(AtkinsonBoore1995.class);
            }
        });
    }
}
