/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Nathan Tolbert
 *******************************************************************************/

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
