package edu.illinois.ncsa.incore.service.dfr3.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static edu.illinois.ncsa.incore.service.dfr3.utils.ValidationUtils.isDemandValid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationUtilsTest {
    @Test
    public void testIsDemandValid() throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("demandDefinition.json");
        String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
        JSONObject demandDefinition = new JSONObject(jsonTxt);
        JSONArray listOfDemands = demandDefinition.getJSONArray("earthquake");

        assertTrue(isDemandValid("0.1 sec sa", "g", listOfDemands).get("demandTypeExisted"));
        assertTrue(isDemandValid("0.1 sa", "g", listOfDemands).get("demandTypeExisted"));
        assertTrue(isDemandValid("sa", "g", listOfDemands).get("demandTypeExisted"));
        assertFalse(isDemandValid("0.1 sec sec sa", "g", listOfDemands).get("demandTypeExisted"));
        assertFalse(isDemandValid("0.1 sec", "g", listOfDemands).get("demandTypeExisted"));
        assertFalse(isDemandValid("sec sa", "g", listOfDemands).get("demandTypeExisted"));
    }

}
