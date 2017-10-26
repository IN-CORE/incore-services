package edu.illinois.ncsa.incore.common.auth;

import edu.illinois.ncsa.incore.common.config.Config;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LdapClient {

    private static final Logger log = Logger.getLogger(LdapClient.class);


    public static String ldapUri = Config.getConfigProperties().getProperty("auth.ldap.url");
    public static String userDn = Config.getConfigProperties().getProperty("auth.ldap.userDn");

    private DirContext getContext() throws NamingException {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUri);
        DirContext context = new InitialDirContext(env);
        return context;
    }

    public Set<String> getUserGroups(String user) {

        Set<String> result = new HashSet<>();
        try {
            DirContext ctx = getContext();
            SearchControls ctls = new SearchControls();
            String[] attrIDs = {"memberof"};
            ctls.setReturningAttributes(attrIDs);
            ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            if (userDn == null) {
                log.error("No auth.ldap.userDn specified in config file");
            }

            NamingEnumeration answer = ctx.search(userDn, "(uid=" + user + ")", ctls);
            while (answer.hasMore()) {
                SearchResult rslt = (SearchResult) answer.next();
                Attributes attrs = rslt.getAttributes();
                String[] groupsDns = attrs.get("memberof").toString().split(", ");


                Set<String> groups = Arrays.stream(groupsDns)
                        .map(this::extractCnFromDn)
                        .collect(Collectors.toSet());

                result.addAll(groups);

            }
            ctx.close();
        } catch (NamingException e) {
            log.error("Could not find groups for user " + user, e);
        }
        return result;
    }

    private String extractCnFromDn(String dn) {
        /* match any non-comma group of characters [^,]
           between the cn= and the next comma
         */
        Pattern p = Pattern.compile("cn=([^,]+),.*");
        Matcher m = p.matcher(dn);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

}