package edu.illinois.ncsa.incore.service.hazard.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.Earthquake;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;
import edu.illinois.ncsa.incore.service.hazard.models.flood.Flood;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.Hurricane;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import org.json.simple.JSONObject;

import jakarta.ws.rs.core.Response;
import java.util.Comparator;
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

    public static Comparator<Earthquake> eqComparator(String sortBy, String order){
        // construct comparator
        Comparator<Earthquake> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Earthquake::getName);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Earthquake::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }

    public static Comparator<Flood> floodComparator(String sortBy, String order){
        // construct comparator
        Comparator<Flood> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Flood::getName);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Flood::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }

    public static Comparator<Hurricane> hurricaneComparator(String sortBy, String order){
        // construct comparator
        Comparator<Hurricane> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Hurricane::getName);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Hurricane::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }

    public static Comparator<Tornado> tornadoComparator(String sortBy, String order){
        // construct comparator
        Comparator<Tornado> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Tornado::getName);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Tornado::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }

    public static Comparator<Tsunami> tsunamiComparator(String sortBy, String order){
        // construct comparator
        Comparator<Tsunami> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Tsunami::getName);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Tsunami::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }
}
