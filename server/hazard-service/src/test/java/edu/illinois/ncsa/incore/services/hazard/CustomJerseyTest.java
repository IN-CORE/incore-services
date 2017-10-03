package edu.illinois.ncsa.incore.services.hazard;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class CustomJerseyTest extends JerseyTest {
    @BeforeAll
    public void before() throws Exception {
        super.setUp();
    }

    @AfterAll
    public void clean() throws Exception {
        super.tearDown();
    }
}
