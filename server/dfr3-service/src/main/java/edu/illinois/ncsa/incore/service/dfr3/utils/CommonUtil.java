package edu.illinois.ncsa.incore.service.dfr3.utils;

import org.json.simple.JSONObject;

public class CommonUtil {
    public static JSONObject createUserStatusJson(String creator, String keyDatabase, int numDfr) {
        JSONObject outJson = new JSONObject();
        outJson.put("creator", creator);
        outJson.put("dfr3_type", keyDatabase);
        outJson.put("total_number_of_entry", numDfr);

        return outJson;
    }
}
