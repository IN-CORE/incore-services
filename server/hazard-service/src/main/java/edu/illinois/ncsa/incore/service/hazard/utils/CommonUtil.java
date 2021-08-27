package edu.illinois.ncsa.incore.service.hazard.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import org.json.simple.JSONObject;

import javax.ws.rs.core.Response;
import java.util.List;

public class CommonUtil {
    // TODO: remove this method once everything uses validateHazardValuesInputs
    public static void validateHazardValuesInput(List<String> demands, List<String> units, IncorePoint point) {
        if (demands == null || units == null || point == null) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Please check if demands, units and location" +
                " are provided for every element in the request json");
        }

        if (demands.size() == 0 || units.size() == 0 || demands.size() != units.size()) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The demands and units for at least one of " +
                "the locations are either missing or not of the same size");
        }
    }

    public static boolean validateHazardValuesInputs(List<String> demands, List<String> units, IncorePoint point)
        throws JsonProcessingException {
        if (demands == null) {
            throw new JsonProcessingException("demands are missing for at least one point") {
            };
        }

        if (units == null || point == null) {
            return false;
        }

        return demands.size() != 0 && units.size() != 0 && demands.size() == units.size();
    }

    public static JSONObject createUserStatusJson(String creator, String keyDatabase, int numHazard) {
        JSONObject outJson = new JSONObject();
        outJson.put("creator", creator);
        outJson.put("hazard_type", keyDatabase);
        outJson.put("total_number_of_hazard", numHazard);

        return outJson;
    }
}
