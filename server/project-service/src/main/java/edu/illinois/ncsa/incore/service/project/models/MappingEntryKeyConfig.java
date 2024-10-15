/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang
 */

package edu.illinois.ncsa.incore.service.project.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class MappingEntryKeyConfig {
    public String unit;
    public String type;
    public String targetColumn;
    public String expression;
}
