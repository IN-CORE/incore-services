/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.users;


import java.util.Set;

public class UserInfo {
    public String login;
    public String email;
    public String firstName;
    public String lastName;
    public String fullName;

    public Set<String> groups;
}
