package edu.illinois.ncsa.incore.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.common.models.HeadersUserGroups;
import jakarta.ws.rs.core.Response;

import java.util.List;

public class UserGroupUtils {
    /***
     * This method receives a user group json, validates it, maps it, and extracts the groups from it. If it fails to map
     * it, then it throws an IncoreHttpException with a detailed message explaining the issue.
     * @param userGroups json string representation of user groups
     * @return array of groups if user group json is valid, throws IncoreHttpException otherwise.
     */
    public static List<String> getUserGroups(String userGroups) {
        if (userGroups == null || !JsonUtils.isJSONValid(userGroups)) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The user-group json provided is invalid.");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HeadersUserGroups groups = objectMapper.readValue(userGroups, HeadersUserGroups.class);
            if (groups.getGroups() == null) {
                throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "User-group is missing the groups field.");
            } else {
                return groups.getGroups();
            }
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Could not map provided user-group.");
        }
    }

    public static boolean isAdmin(List<String> groups) {
        String admin_group = "incore_admin";
        if (groups.contains(admin_group)){
            return true;
        }

        return false;
    }
}
