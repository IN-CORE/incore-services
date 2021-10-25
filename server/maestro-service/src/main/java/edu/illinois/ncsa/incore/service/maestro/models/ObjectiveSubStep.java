/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class ObjectiveSubStep extends SubStep {
    // this is for 3. Determine Goals & Objectives
    public List<Hazard> hazards;
    public List<Input> inputs;
    public List<Workflow> workflows;
}
