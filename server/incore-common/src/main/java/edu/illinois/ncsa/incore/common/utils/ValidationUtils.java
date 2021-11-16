package edu.illinois.ncsa.incore.common.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ValidationUtils {
    // validate demand type matches demand unit or not
    public static List<Boolean> isDemandValid(String demandType, String demandUnit, String hazardType,
                                              JSONObject demandDefinition) {

        JSONArray listOfDemands = demandDefinition.getJSONArray(hazardType);
        AtomicBoolean demandTypeExisted = new AtomicBoolean(false);
        AtomicBoolean demandUnitAllowed = new AtomicBoolean(false);
        listOfDemands.forEach(entry -> {
            // check first if demand type exist
            if (((JSONObject) entry).get("demand_type").toString().toLowerCase(Locale.ROOT).equals(demandType)) {
                demandTypeExisted.set(true);

                // check if demand unit is allowed
                JSONArray allowedDemandUnits = ((JSONObject) entry).getJSONArray("demand_unit");
                allowedDemandUnits.forEach(unit -> {
                    if (unit.toString().toLowerCase(Locale.ROOT).equals(demandUnit)) {
                        demandUnitAllowed.set(true);
                    }
                });
            }
        });
        return Arrays.asList(demandTypeExisted.get(), demandUnitAllowed.get());
    }
}
