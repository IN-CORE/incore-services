/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.dataset;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.Concept;

import java.util.List;

public class Schema {
    public String name;
    public String description;
    public List<Concept> concepts;
}
