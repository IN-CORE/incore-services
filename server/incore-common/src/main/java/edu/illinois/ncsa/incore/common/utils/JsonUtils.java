package edu.illinois.ncsa.incore.common.utils;

import edu.illinois.ncsa.incore.common.AllocationConstants;
import edu.illinois.ncsa.incore.common.dao.IGroupAllocationsRepository;
import edu.illinois.ncsa.incore.common.dao.IUserFinalQuotaRepository;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.dao.IUserAllocationsRepository;
import edu.illinois.ncsa.incore.common.models.*;
import jakarta.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

public class JsonUtils {
    public static final Logger logger = Logger.getLogger(JsonUtils.class);

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
                for (Object jObj : inArray) {
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

    public static String parseUserName(String userInfo) {
        String userName = null;
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject userInfoJson = (org.json.simple.JSONObject) parser.parse(userInfo);
            userName = (String) userInfoJson.get("preferred_username");

        } catch (ParseException e) {
            logger.error(AllocationConstants.UNABLE_TO_PARSE_TOKEN, e);
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, AllocationConstants.UNABLE_TO_PARSE_TOKEN);
        } catch (NullPointerException e) {
            logger.error(AllocationConstants.ALLOCTION_ENDPOINT_NO_USERGROUP, e);
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, AllocationConstants.ALLOCTION_ENDPOINT_NO_USERINFO);
        }

        return userName;
    }

    public static Boolean isLoggedInUserAdmin(String userGroup){
        Boolean isAdmin = false;
        String userGRoup = null;
        try {
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject userGroupJson = (org.json.simple.JSONObject) parser.parse(userGroup);
            org.json.simple.JSONArray groups = (org.json.simple.JSONArray) userGroupJson.get("groups");
            isAdmin = groups.toString().contains("\"incore_admin\"");
        } catch (ParseException e) {
            logger.error(AllocationConstants.UNABLE_TO_PARSE_TOKEN, e);
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, AllocationConstants.UNABLE_TO_PARSE_TOKEN);
        } catch (NullPointerException e) {
            logger.error(AllocationConstants.ALLOCTION_ENDPOINT_NO_USERGROUP, e);
            throw new IncoreHTTPException(Response.Status.FORBIDDEN, AllocationConstants.ALLOCTION_ENDPOINT_NO_USERGROUP);
        }

        return isAdmin;
    }
}
