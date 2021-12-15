package edu.illinois.ncsa.incore.service.dfr3.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class ValidationUtils {
    // validate demand type matches demand unit or not
    public static HashMap<String, Boolean> isDemandValid(String demandType, String demandUnit,
                                                         JSONArray listOfDemands) {


        AtomicBoolean demandTypeExisted = new AtomicBoolean(false);
        AtomicBoolean demandUnitAllowed = new AtomicBoolean(false);

        listOfDemands.forEach(entry -> {
            String demandTypeEvaluated = demandType;

            // check first if demand type exist
            // sa and sd are special cases
            if (demandType.contains("sa") || demandType.contains("sd") || demandType.contains("sv")) {
                String[] demandTypePhrase = demandType.split("\\s+");

                // check if first word is positive numeric value
                Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
                if (demandTypePhrase.length > 0 && demandTypePhrase.length <= 3
                    && pattern.matcher(demandTypePhrase[0]).matches()
                    && (demandTypePhrase[demandTypePhrase.length - 1].equals("sa")
                    || demandTypePhrase[demandTypePhrase.length - 1].equals("sd"))) {

                    // replace demand type 0.1 sec Sa with just sa
                    demandTypeEvaluated = demandTypePhrase[demandTypePhrase.length - 1];
                }
            }

            if (((JSONObject) entry).get("demand_type").toString().toLowerCase().equals(demandTypeEvaluated)) {
                demandTypeExisted.set(true);

                // check if demand unit is allowed
                JSONArray allowedDemandUnits = ((JSONObject) entry).getJSONArray("demand_unit");
                allowedDemandUnits.forEach(unit -> {
                    if (unit.toString().toLowerCase().equals(demandUnit)) {
                        demandUnitAllowed.set(true);
                    }
                });

            }


        });

        HashMap<String, Boolean> validation = new HashMap<>();
        validation.put("demandTypeExisted", demandTypeExisted.get());
        validation.put("demandUnitAllowed", demandUnitAllowed.get());

        return validation;
    }
}
