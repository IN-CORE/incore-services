/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations;

import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EarthquakeModel;
import edu.illinois.ncsa.incore.service.hazard.models.eq.EqParameters;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Site;
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
public class AtkinsonBoore1995Test {

    private AtkinsonBoore1995 attenuation;

    @BeforeAll
    public void before() throws Exception {
        double latitude = 35.927;
        double longitude = -89.919;
        double focaldepth = 10.0;
        double magnitude = 7.9;

        EqParameters ruptureParameters = new EqParameters();
        ruptureParameters.setMagnitude(magnitude);
        ruptureParameters.setSrcLatitude(latitude);
        ruptureParameters.setSrcLongitude(longitude);
        ruptureParameters.setDepth(focaldepth);

        attenuation = new AtkinsonBoore1995();
        attenuation.setRuptureParameters(ruptureParameters);
    }

    @Test
    public void testGetValuesFromHazardCalc() throws Exception {
        String period = "0.2";
        String demand = HazardUtil.SA;
        double latitude = 35.07899;
        double longitude = -90.017;

        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        Map<BaseAttenuation, Double> attenuations = new HashMap<>();
        attenuations.put(attenuation, 1.0);

        // TODO this should come from a mock eq
        Earthquake eq = new EarthquakeModel();
        SeismicHazardResult result = HazardCalc.getGroundMotionAtSite(eq, attenuations, site, period, demand, HazardUtil.units_g,
                0, true, null, "incrtest", "{\"groups\": [\"incore_user\"]}");

        double expected = 0.5322;
        assertEquals(expected, result.getHazardValue(), expected * 0.05);
        assertEquals(result.getDemand(), HazardUtil.SA);
        assertEquals(result.getPeriod(), period);
        assertEquals(result.getUnits(), HazardUtil.units_g);

        // Test PGD from PGA
        period = "0.0";
        demand = HazardUtil.PGD;
        SeismicHazardResult result_pgd = HazardCalc.getGroundMotionAtSite(eq, attenuations, site, period, demand, HazardUtil.units_in,
                0, true, null, "incrtest", "{\"groups\": [\"incore_user\"]}");

        double expected_pgd = 3.269564;
        assertEquals(expected_pgd, result_pgd.getHazardValue(), expected * 0.05);
        assertEquals(result_pgd.getDemand(), HazardUtil.PGD);
        assertEquals(result_pgd.getPeriod(), period);
        assertEquals(result_pgd.getUnits(), HazardUtil.units_in);
    }

    @Test
    public void testGetValue() throws Exception {
        String period = "0.2";
        double latitude = 35.07899;// 35.927;
        double longitude = -90.017;// -90.05;

        GeometryFactory factory = new GeometryFactory();
        Site site = new Site(factory.createPoint(new Coordinate(longitude, latitude)));

        double hazard = attenuation.getValue(period, site);

        double expected = 0.3502;
        assertEquals(expected, hazard, expected * 0.05);
    }
}
