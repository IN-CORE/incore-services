/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Omar Elabd (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.dao;

import edu.illinois.ncsa.incore.common.models.Space;

import java.util.List;

public interface ISpaceRepository {
    void initialize();
    List<Space> getAllSpaces();
    Space addSpace(Space space);
    Space getSpaceById(String id);
    Space getSpaceByName(String name);
    Space deleteSpace(String id);
}
