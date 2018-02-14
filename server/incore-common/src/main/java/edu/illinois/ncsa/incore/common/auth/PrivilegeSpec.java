package edu.illinois.ncsa.incore.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A definition of privileges, which are applied to a single
 * entity in incore. The PrivilegeSpec and Authorizer don't
 * know anything about the actual entity. They only care about
 * what the PrivilegeSpec indicates about who is allowed to access it.
 */
public class PrivilegeSpec {

    private static final Logger log = Logger.getLogger(PrivilegeSpec.class);


    public Map<String,Privilege> userPrivileges;
    public Map<String,Privilege> groupPrivileges;

    public PrivilegeSpec() {
        userPrivileges = new HashMap<String, Privilege>();
        groupPrivileges = new HashMap<String, Privilege>();
    }

    public static PrivilegeSpec fromJson(String privilegeSpecJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(privilegeSpecJson, PrivilegeSpec.class);
        } catch (Exception e) {
            log.error("Could not parse privilegeSpec JSON. Returning empty PrivilegeSpec", e);
            return new PrivilegeSpec();
        }
    }

    public static PrivilegeSpec newWithSingleOwner(String owner) {
        PrivilegeSpec privilegeSpec = new PrivilegeSpec();
        privilegeSpec.userPrivileges.put(owner, Privilege.ADMIN);
        return privilegeSpec;
    }

}
