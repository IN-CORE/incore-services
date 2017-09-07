package edu.illinois.ncsa.incore.services.fragility.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.fragility.FragilityController;
import edu.illinois.ncsa.incore.services.fragility.dto.MappingRequest;
import edu.illinois.ncsa.incore.services.fragility.dto.MappingSubject;
import edu.illinois.ncsa.incore.services.fragility.dto.SchemaType;
import org.geojson.FeatureCollection;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FragilityTest extends CustomJerseyTest {
    public FragilityTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(FragilityController.class);

        return application;
    }

    @Test
    public void testMappingFunction() throws IOException {
        // arrange
        URL jsonURL = this.getClass().getClassLoader().getResource("json/inventory.json");

        FeatureCollection featureCollection = new ObjectMapper().readValue(jsonURL, FeatureCollection.class);

        MappingSubject subject = new MappingSubject(SchemaType.Building, featureCollection);

        MappingRequest request = new MappingRequest(subject);

        // act
        Response response = target("/fragility/select").request()
                                                            .accept(MediaType.APPLICATION_JSON)
                                                            .post(Entity.json(request));

        // assert
        assertEquals("", "");
    }
}
