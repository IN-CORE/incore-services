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

    public static JSONObject createUserFinalQuotaJson(String username, IUserFinalQuotaRepository finalQuotaRepository) throws ParseException{
        UserFinalQuota quota = finalQuotaRepository.getQuotaByUsername(username);   // get default allocation
        JSONObject outJson = new JSONObject();
        UserUsages limit = new UserUsages();
        if (quota != null) {
            // get user's limit
            limit = quota.getApplicableLimits();
        } else {
            // when the quota is null, give the default quota information
            limit = AllocationUtils.setDefalutLimit(limit);
        }

        outJson = setUsageJson(username, limit, false);

        return outJson;
    }

    public static JSONObject createUserUsageJson(String username, IUserAllocationsRepository allocationRepository) throws ParseException{
        UserAllocations allocation = allocationRepository.getAllocationByUsername(username);   // get default allocation
        UserUsages usage = new UserUsages();
        JSONObject outJson = new JSONObject();

        if (allocation != null) {
            // get user's usage status
            usage = allocation.getUsage();
            outJson = setUsageJson(username, usage, false);
        } else {
            outJson = setNotFoundJson(username);
        }

        return outJson;
    }

    public static JSONObject createGroupAllocationJson(String groupname, IGroupAllocationsRepository allocationsRepository) throws ParseException{
        GroupAllocations allocation = allocationsRepository.getAllocationByGroupname(groupname);   // get default allocation
        UserUsages limit = new UserUsages();
        JSONObject outJson = new JSONObject();

        if (allocation != null) {
            // get user's allocation from user final quota
            limit = allocation.getLimits();
            outJson = setUsageJson(groupname, limit, true);
        } else {
            // get default allocation
            outJson = setNotFoundJson(groupname);
        }

        return outJson;
    }

    public static JSONObject setNotFoundJson(String username) {
        JSONObject outJson = new JSONObject();
        outJson.put("ID", username);
        outJson.put("reason_or_error", "ID not found");

        return outJson;
    }

    public static JSONObject setUsageJson(String username, UserUsages usage, Boolean isGroup) {
        long dataset_file_size = usage.getDatasetSize();
        long hazard_file_size = usage.getHazardDatasetSize();

        double dataset_size_kb = dataset_file_size / 1024;
        double dataset_size_mb = dataset_size_kb / 1024;
        double dataset_size_gb = dataset_size_mb / 1024;

        double hazard_size_kb = hazard_file_size / 1024;
        double hazard_size_mb = hazard_size_kb / 1024;
        double hazard_size_gb = hazard_size_mb / 1024;

        // round values
        dataset_size_kb = Math.round(dataset_size_kb * 100.0) / 100.0;
        dataset_size_mb = Math.round(dataset_size_mb * 100.0) / 100.0;
        dataset_size_gb = Math.round(dataset_size_gb * 100.0) / 100.0;
        hazard_size_kb = Math.round(hazard_size_kb * 100.0) / 100.0;
        hazard_size_mb = Math.round(hazard_size_mb * 100.0) / 100.0;
        hazard_size_gb = Math.round(hazard_size_gb * 100.0) / 100.0;

        String out_dataset_size;
        String out_hazard_size;

        if (dataset_size_gb >= 1) {
            out_dataset_size = dataset_size_gb + " GB";
        } else if (dataset_size_mb >= 1) {
            out_dataset_size = dataset_size_mb + " MB";
        } else {
            out_dataset_size = dataset_size_kb + " KB";
        }

        if (hazard_size_gb >= 1) {
            out_hazard_size = hazard_size_gb + " GB";
        } else if (hazard_size_mb >= 1) {
            out_hazard_size = hazard_size_mb + " MB";
        } else {
            out_hazard_size = hazard_size_kb + " KB";
        }

        JSONObject outJson = new JSONObject();
        if (isGroup) {
            outJson.put("group", username);
        } else {
            outJson.put("user", username);
        }
        outJson.put("total_number_of_datasets", usage.getDatasets());
        outJson.put("total_number_of_hazards", usage.getHazards());
        outJson.put("total_number_of_hazard_datasets", usage.getHazardDatasets());
        outJson.put("total_number_of_dfr3", usage.getDfr3());
        outJson.put("total_file_size_of_datasets", out_dataset_size);
        outJson.put("total_file_size_of_datasets_byte", dataset_file_size);
        outJson.put("total_file_size_of_hazard_datasets", out_hazard_size);
        outJson.put("total_file_size_of_hazard_datasets_byte", usage.getHazardDatasetSize());
        outJson.put("service", usage.getService());
        outJson.put("incoreLab", usage.getIncoreLab().toJson());

        return outJson;
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
