package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.*;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.SeismicHazardResult;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardCalc;
import edu.illinois.ncsa.incore.service.hazard.models.eq.utils.HazardUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SadighChangEganMakdisiYoung1997Test {

    private SadighChangEganMakdisiYoung1997 attenuation;

    @BeforeAll
    public void before() throws Exception {
        double latitude = 35.201395;
        double longitude = -89.971332;
        double focaldepth = 10.0;
        double magnitude = 7.9;

        Map<String, String> faultTypeMap = new HashMap<>();
        faultTypeMap.put(SadighChangEganMakdisiYoung1997.class.getSimpleName(), FaultMechanism.NORMAL);

        EqParameters ruptureParameters = new EqParameters();
        ruptureParameters.setFaultTypeMap(faultTypeMap);
        ruptureParameters.setMagnitude(magnitude);
        ruptureParameters.setSrcLatitude(latitude);
        ruptureParameters.setSrcLongitude(longitude);
        ruptureParameters.setDepth(focaldepth);
        ruptureParameters.setDipAngle(90.0);
        ruptureParameters.setAzimuthAngle(130.0);

        attenuation = new SadighChangEganMakdisiYoung1997();
        attenuation.setRuptureParameters(ruptureParameters);
    }

    @Test
    public void testGetValuesFromHazardCalc() throws Exception {
        // Tests getting the amplified hazard for the attenuation model based on soil type
        String period = "0.0";
        String demand = HazardUtil.PGA;
        double latitude = 35.140197;
        double longitude = -90.025849;

        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        Map<BaseAttenuation, Double> attenuations = new HashMap<>();
        attenuations.put(attenuation, 1.0);

        Earthquake eq = new EarthquakeModel();
        SeismicHazardResult result = HazardCalc.getGroundMotionAtSite(eq, attenuations, site, period, demand, HazardUtil.units_g,
            0, true, null, "incrtest", "{\"groups\": [\"incore_user\"]}");

        double expected = 0.6945694;
        assertEquals(expected, result.getHazardValue(), expected * 0.05);
        assertEquals(result.getDemand(), HazardUtil.PGA);
        assertEquals(result.getPeriod(), period);
        assertEquals(result.getUnits(), HazardUtil.units_g);
    }

    @Test
    public void testGetValue() throws Exception {
        // Tests the unamplified value from the attenuation model
        String period = "PGA";
        double latitude = 35.140197;
        double longitude = -90.025849;

        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        double hazard = attenuation.getValue(period, site);

        double expected = 0.4945928;
        assertEquals(expected, hazard, expected * 0.05);
    }

    @Test
    public void testGetStandardDeviation() throws Exception {
        String period = "PGA";
        double latitude = 35.140197;
        double longitude = -90.025849;

        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        double hazard = attenuation.getValue(period, site);
        double standardDeviation = attenuation.getStandardDeviation(hazard, period, site);
        double expected = 0.3806;
        assertEquals(expected, standardDeviation, expected * 0.05);
    }
}
