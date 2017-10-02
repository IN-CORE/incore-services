package edu.illinois.ncsa.incore.common.auth;

import java.util.HashMap;
import java.util.Map;

public class PrivilegeSpec {
    public Map<String,Privilege> userPrivileges;
    public Map<String,Privilege> groupPrivileges;

    public PrivilegeSpec() {
        userPrivileges = new HashMap<String, Privilege>();
        groupPrivileges = new HashMap<String, Privilege>();
    }

    public PrivilegeSpec(String privilegeSpecJson) {

    }
}
