package edu.illinois.ncsa.incore.services.fragility.test;

import edu.illinois.ncsa.incore.services.fragility.FragilityMappingController;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FragilityMappingControllerTest extends CustomJerseyTest {
    public FragilityMappingControllerTest() {
        super();
    }

    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication(FragilityMappingController.class);

        return application;
    }

    @Test
    public void testSimpleMapping() throws UnsupportedEncodingException {
        //language=json
        String json = "{\n" +
            "    \"no_stories\": 5,\n" +
            "    \"year_built\": 1990,\n" +
            "    \"Soil\": \"Upland\",\n" +
            "    \"occ_type\": \"COM4\",\n" +
            "    \"struct_typ\": \"C1\",\n" +
            "    \"retrofit\": \"Non-Retrofit Fragility ID Code\"\n" +
            "}";

        String url = URLEncoder.encode(json, "UTF-8").replace("+", "%20");

        String output = target("/mapping/byJson").queryParam("json", url)
                                                 .request()
                                                 .accept(MediaType.APPLICATION_JSON_TYPE)
                                                 .get(String.class);

        JSONObject parsedObject = new JSONObject(output);
        Object fragilityId = parsedObject.get("fragilityId");

        assertEquals("STR_C1_5", fragilityId);
    }
}
