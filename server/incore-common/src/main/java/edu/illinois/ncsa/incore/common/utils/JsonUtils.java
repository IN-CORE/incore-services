package edu.illinois.ncsa.incore.common.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class JsonUtils {
    // validate if json is okay
    public static boolean isJSONValid(String inJson) {
        try {
            new JSONObject(inJson);
        } catch (JSONException ex) {
            try {
                new JSONArray(inJson);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static String extractValueFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        if (jsonObj.has(inId)) {
            Object output = jsonObj.get(inId);
            return output.toString();
        } else {
            return "";
        }
    }

    public static List<String> extractValueListFromJsonString(String inId, String inJson) {
        JSONObject jsonObj = new JSONObject(inJson);
        List<String> outList = new LinkedList<String>();
        if (jsonObj.has(inId)) {
            try {
                JSONArray inArray = (JSONArray) jsonObj.get(inId);
                for (Object jObj: inArray) {
                    outList.add(jObj.toString());
                }
                return outList;
            } catch (JSONException e) {
                return outList;
            }
        } else {
            return outList;
        }
    }
}
