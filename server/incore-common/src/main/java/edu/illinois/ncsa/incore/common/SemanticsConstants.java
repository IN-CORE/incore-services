package edu.illinois.ncsa.incore.common;

import java.util.Arrays;
import java.util.List;

public class SemanticsConstants {

    public static final List<String> RESERVED_COLUMNS = Arrays.asList("num_stories", "period");
    public static final String SEMANTICS_ENDPOINT = "semantics/api/types";

    // Auth headers
    public static final String X_AUTH_USERINFO = "x-auth-userinfo";
    public static final String X_AUTH_USERGROUP = "x-auth-usergroup";
}
