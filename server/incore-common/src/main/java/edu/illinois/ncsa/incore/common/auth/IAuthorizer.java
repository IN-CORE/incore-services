/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Nathan Tolbert
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.common.auth;

import java.util.Set;

public interface IAuthorizer {
    Set<PrivilegeLevel> getPrivilegesFor(String user, Privileges spec);

    Set<PrivilegeLevel> getPrivilegesFor(String user, String privilegeSpecJson);

    boolean canRead(String user, Privileges privileges);

    boolean canRead(String user, String privilegeSpecJson);

    boolean canWrite(String user, Privileges privileges);

    boolean canWrite(String user, String privilegeSpecJson);
}
