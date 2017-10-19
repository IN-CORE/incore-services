package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.service.hazard.CustomJerseyTest;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EqParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AtkinsonBoore1995Test extends CustomJerseyTest {

    private AtkinsonBoore1995 attenuation;

    @BeforeAll
    public void before() throws Exception {
        super.setUp();
        double latitude = 35.927;
        double longitude = -89.919;
        double focaldepth = 10.0;
        double magnitude = 7.9;

        EqParameters ruptureParameters = new EqParameters();
        ruptureParameters.setMagnitude(magnitude);
        ruptureParameters.setSrcLatitude(latitude);
        ruptureParameters.setSrcLongitude(longitude);

        attenuation.setRuptureParameters(ruptureParameters);
    }

    @Test
    public void testGetValue() throws Exception {
        String period = "0.2 SA";
        double latitude = 35.07899;// 35.927;
        double longitude = -90.017;// -90.05;


        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        double hazard = attenuation.getValue(period, site);

        double expected = 0.3502;
        assertEquals(expected, hazard, expected * 0.05);
    }
}
