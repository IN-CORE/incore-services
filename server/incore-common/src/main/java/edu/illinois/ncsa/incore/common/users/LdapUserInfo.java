/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.users;


import java.util.Set;

public class LdapUserInfo {
    public String login;
    public String email;
    public String firstName;
    public String lastName;
    public String fullName;

    public Set<String> groups;
}
