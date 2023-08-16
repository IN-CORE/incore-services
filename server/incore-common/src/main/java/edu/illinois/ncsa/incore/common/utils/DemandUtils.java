package edu.illinois.ncsa.incore.common.utils;

import edu.illinois.ncsa.incore.common.models.DemandDefinition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DemandUtils {
    public static List<DemandDefinition> getAllowedDemands(JSONObject demandDefinition, String hazardType) {
        JSONArray listOfDemands = demandDefinition.getJSONArray(hazardType);
        List<DemandDefinition> demandList = new ArrayList<>();
        for (int i = 0; i < listOfDemands.length(); i++) {
            JSONObject demandJson = listOfDemands.getJSONObject(i);
            DemandDefinition demand = new DemandDefinition();
            demand.setDemand_type(demandJson.getString("demand_type"));
            demand.setDescription(demandJson.getString("description"));
            JSONArray demandUnits = demandJson.getJSONArray("demand_unit");
            List<String> allowedDemandUnits = new ArrayList<>();
            demandUnits.forEach(unit -> {
                allowedDemandUnits.add(unit.toString());
            });
            demand.setDemand_unit(allowedDemandUnits);
            demandList.add(demand);
        }

        return demandList;
    }
}
