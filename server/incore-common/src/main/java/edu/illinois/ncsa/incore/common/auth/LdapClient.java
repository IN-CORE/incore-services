/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.common.auth;

import edu.illinois.ncsa.incore.common.config.Config;
import edu.illinois.ncsa.incore.common.users.LdapUserInfo;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Groups{
    private long lastUserRefresh = 0;
    private Set<String> userGroups;

    public long getLastUserRefresh() {
        return lastUserRefresh;
    }

    public void setLastUserRefresh(long lastUserRefresh) {
        this.lastUserRefresh = lastUserRefresh;
    }

    public Set<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Set<String> userGroups) {
        this.userGroups = userGroups;
    }
}

public class LdapClient {

    private static final Logger log = Logger.getLogger(LdapClient.class);


    public static String ldapUri = Config.getConfigProperties().getProperty("auth.ldap.url");
    public static String userDn = Config.getConfigProperties().getProperty("auth.ldap.userDn");
    public static long ldapRefreshSecs =
        Long.parseLong(Config.getConfigProperties().getProperty("auth.ldap.cache.refresh.secs"));

    public Map<String,Groups> userGroupCache = new HashMap<>();

    private DirContext getContext() throws NamingException {

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUri);
        DirContext context = new InitialDirContext(env);
        return context;
    }

    public Set<String> getUserGroups(String user) {
        long currSecs = System.currentTimeMillis()/1000;

        if ( userGroupCache.containsKey(user)) {
            if (currSecs - userGroupCache.get(user).getLastUserRefresh() < ldapRefreshSecs) {
                System.out.println("Cache being used. Last Refresh: "+ userGroupCache.get(user).getLastUserRefresh() +
                    " Curr Time: " + currSecs);
                return userGroupCache.get(user).getUserGroups();
            }
        }

        Groups groupsInfo = new Groups();
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

        groupsInfo.setUserGroups(result);
        groupsInfo.setLastUserRefresh(System.currentTimeMillis()/1000);

        if (userGroupCache.containsKey(user)) {
            System.out.println("Cache Refreshed. Last Refresh: " + userGroupCache.get(user).getLastUserRefresh() +
                " Curr Time: " + currSecs);
        } else {
            System.out.println("Added a new user to cache for first time");
        }
        userGroupCache.put(user, groupsInfo);

        return result;
    }

    public LdapUserInfo getUserInfoFor(String user) {
        LdapUserInfo userInfo = null;
        try {
            DirContext ctx = getContext();
            SearchControls ctls = new SearchControls();
            String[] attrIDs = {"givenname", "sn", "mail", "cn"};
            ctls.setReturningAttributes(attrIDs);
            ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            if (userDn == null) {
                log.error("No auth.ldap.userDn specified in config file");
            }

            NamingEnumeration answer = ctx.search(userDn, "(uid=" + user + ")", ctls);
            while (answer.hasMore()) {
                SearchResult rslt = (SearchResult) answer.next();
                Attributes attrs = rslt.getAttributes();



                userInfo = new LdapUserInfo();
                userInfo.firstName = attrs.get("givenname").get().toString();
                userInfo.lastName = attrs.get("sn").get().toString();
                userInfo.fullName = attrs.get("cn").get().toString();
                userInfo.email = attrs.get("mail").get().toString();
                userInfo.login = user;
                userInfo.groups = getUserGroups(user);


            }
            ctx.close();
        } catch (NamingException e) {
            log.error("Could not find groups for user " + user, e);
        }
        if (userInfo == null) {
            log.error("Could not find user " + user);
        }
        return userInfo;

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
