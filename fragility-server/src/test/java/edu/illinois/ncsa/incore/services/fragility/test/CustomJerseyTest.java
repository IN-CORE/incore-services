package edu.illinois.ncsa.incore.services.fragility.test;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public abstract class CustomJerseyTest extends JerseyTest {
    private String packageName;

    public CustomJerseyTest(String controllerPackageName) {
        this.packageName = controllerPackageName;
    }

    public CustomJerseyTest(Class klass) {
        this.packageName = klass.getPackage().getName();
    }

    /**
     * Boilerplate to configure the resource controller to test
     */
    @Override
    public ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockApplication application = new MockApplication();

        return application;
    }

    /**
     * Boilerplate to give it a servlet container.
     * This may not be necessary unless the controller uses the @Context
     * annotation to get the ServletContext
     */
    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new TestContainerFactory() {
            @Override
            public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
                return new TestContainer() {
                    private HttpServer server;

                    @Override
                    public ClientConfig getClientConfig() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return baseUri;
                    }

                    @Override
                    public void start() {
                        try {
                            this.server = GrizzlyWebContainerFactory.create(
                                baseUri, Collections.singletonMap("jersey.config.server.provider.packages",
                                                                  packageName)
                            );
                        } catch (ProcessingException e) {
                            throw new TestContainerException(e);
                        } catch (IOException e) {
                            throw new TestContainerException(e);
                        }
                    }

                    @Override
                    public void stop() {
                        this.server.stop();
                    }
                };
            }
        };
    }

    @BeforeAll
    public void before() throws Exception {
        super.setUp();
    }

    @AfterAll
    public void clean() throws Exception {
        super.tearDown();
    }
}
