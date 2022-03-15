/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.dao;

import edu.illinois.ncsa.incore.common.models.GroupAllocations;

import java.util.List;

public interface IGroupAllocationsRepository {
    void initialize();

    List<GroupAllocations> getAllAllocations();

    GroupAllocations getAllocationById(String id);

    GroupAllocations getAllocationByUsername(String username);

    GroupAllocations addAllocation(GroupAllocations allocation);
}
