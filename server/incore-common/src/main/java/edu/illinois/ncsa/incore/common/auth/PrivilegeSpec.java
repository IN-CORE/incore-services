package edu.illinois.ncsa.incore.common.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * A definition of privileges, which are applied to a single
 * entity in incore. The PrivilegeSpec and Authorizer don't
 * know anything about the actual entity. They only care about
 * what the PrivilegeSpec indicates about who is allowed to access it.
 */
public class PrivilegeSpec {
    public Map<String,Privilege> userPrivileges;
    public Map<String,Privilege> groupPrivileges;

    public PrivilegeSpec() {
        userPrivileges = new HashMap<String, Privilege>();
        groupPrivileges = new HashMap<String, Privilege>();
    }

    public PrivilegeSpec(String privilegeSpecJson) {
        JSONObject json = new JSONObject(privilegeSpecJson);

    }
}
