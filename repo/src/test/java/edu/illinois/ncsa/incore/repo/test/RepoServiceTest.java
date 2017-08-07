package edu.illinois.ncsa.incore.repo.test;

/**
 * Created by ywkim on 8/7/2017.
 */
//import edu.illinois.ncsa.incore.services.fragilitymapping.FragilityMappingController;
import edu.illinois.ncsa.incore.repo.RepoService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class RepoServiceTest extends JerseyTest {
    /**
     * Boilerplate to configure the resource controller to test
     * @return
     */
    @Override
    public Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new ResourceConfig(RepoService.class);
    }


//    /**
//     * Boilerplate to give it a servlet container.
//     * This may not be necessary unless the controller uses the @Context
//     * annotation to get the ServletContext
//     * @return
//     * @throws TestContainerException
//     */
//    @Override
//    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
//        return new TestContainerFactory() {
//            @Override
//            public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
//                return new TestContainer() {
//                    private HttpServer server;
//
//                    @Override
//                    public ClientConfig getClientConfig() {
//                        return null;
//                    }
//
//                    @Override
//                    public URI getBaseUri() {
//                        return baseUri;
//                    }
//
//                    @Override
//                    public void start() {
//                        try {
//                            this.server = GrizzlyWebContainerFactory.create(
//                                    baseUri, Collections.singletonMap("jersey.config.server.provider.packages", "edu.illinois.ncsa.incore.repo")
//                            );
//                        } catch (ProcessingException e) {
//                            throw new TestContainerException(e);
//                        } catch (IOException e) {
//                            throw new TestContainerException(e);
//                        }
//                    }
//
//                    @Override
//                    public void stop() {
//                        this.server.stop();
//                    }
//                };
//            }
//        };
//    }


    @Test
    public void testSimpleMapping() throws UnsupportedEncodingException {
//        String url = URLEncoder.encode(
//                "{\"no_stories\":5,\"year_built\":1990,\"Soil\":\"Upland\",\"occ_type\":\"COM4\",\"struct_typ\":\"C1\",\"retrofit\":\"Non-Retrofit Fragility ID Code\"}",
//                "UTF-8").replace("+", "%20");
        // test
        String output = target("/datasets/query").
                queryParam("type", "edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0").
                request().accept(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).get(String.class);
        System.out.println("----------output is:" + output);
        JSONObject parsed = new JSONObject(output);
        Object fragId = parsed.get("fragilityId");
        assertEquals("STR_C1_5", fragId);
    }
}
