package edu.illinois.ncsa.incore.common.auth;

import java.util.HashSet;
import java.util.Set;

public class Authorizer {

    private static Authorizer instance;

    //I don't really like the idea of doing this as a singleton,
    //this thing is going to need some configuration to know what
    //ldap it connects to, etc. Some sort of configuration or dependency injection
    //might be preferable, but that's not going to happen for the prototype
    public static Authorizer getInstance() {
        if (instance == null) {
            instance = new Authorizer();
        }
        return instance;
    }


    public Set<Privilege> getPrivilegesFor(String user, PrivilegeSpec spec) {
        Set<Privilege> privs = getUserSpecificPrivileges(user, spec);
        privs.addAll(getGroupSpecificPrivileges(user, spec));
        return privs;
    }



    public Set<Privilege> getPrivilegesFor(String user, String privilegeSpecJson) {
        PrivilegeSpec spec = new PrivilegeSpec(privilegeSpecJson);
        return getPrivilegesFor(user, spec);
    }

    ////////////////////////////////////////////////////////
    // convenience methods to make it easier to query
    /////////////////////////////////////////////////////////

    public boolean canRead(String user, PrivilegeSpec privilegeSpec) {
        return getPrivilegesFor(user, privilegeSpec).contains(Privilege.READ);
    }

    public boolean canRead(String user, String privilegeSpecJson) {
        return getPrivilegesFor(user, privilegeSpecJson).contains(Privilege.READ);
    }

    public boolean canWrite(String user, PrivilegeSpec privilegeSpec) {
        return getPrivilegesFor(user, privilegeSpec).contains(Privilege.WRITE);
    }

    public boolean canWrite(String user, String privilegeSpecJson) {
        return getPrivilegesFor(user, privilegeSpecJson).contains(Privilege.WRITE);
    }


    private Set<Privilege> getGroupSpecificPrivileges(String user, PrivilegeSpec spec) {
        return new HashSet<Privilege>();
    }

    private Set<Privilege> getUserSpecificPrivileges(String user, PrivilegeSpec spec) {
        return new HashSet<Privilege>();
    }
}
