/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.common.Enumeration;
import edu.illinois.ncsa.incore.semantic.metamodel.concepts.EnumerableConcept;

import java.util.ArrayList;
import java.util.List;

public class EnumerableAttribute<T> extends Attribute<T> {
    public EnumerableConcept conceptReference;
    public List<Enumeration<T>> enumerations = new ArrayList<>();

    public EnumerableAttribute() {}

    public EnumerableAttribute(String name, String description, List<Enumeration<T>> enumerations) {
        super.name = name;
        super.description = description;
        this.enumerations = enumerations;

        super.isUnique = false;
    }

    @Override
    public EnumerableConcept getConceptReference() {
        return conceptReference;
    }
}
