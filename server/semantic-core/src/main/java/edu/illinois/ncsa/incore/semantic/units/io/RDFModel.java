/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public final class RDFModel {
    private RDFModel() {
    }

    // URI - Instances
    public static final String unitURI = "http://tools.in-core.org/semantic/elements/units/";
    public static final String dimensionURI = "http://tools.in-core.org/semantic/elements/dimensions/";
    public static final String prefixURI = "http://tools.in-core.org/semantic/elements/prefixes/";

    // URI - OWL Schema
    public static final String schemaURI = "http://tools.in-core.org/semantic/schema/units/";

    // Properties - Units
    public static final Property symbol = new PropertyImpl(schemaURI, "symbol");
    public static final Property plural = new PropertyImpl(schemaURI, "plural");
    public static final Property symbolUnicode = new PropertyImpl(schemaURI, "utf_symbol");
    public static final Property pluralUnicode = new PropertyImpl(schemaURI, "utf_plural");
    public static final Property unitSystem = new PropertyImpl(schemaURI, "unit_system");
    public static final Property prefix = new PropertyImpl(schemaURI, "prefix");
    public static final Property baseUnit = new PropertyImpl(schemaURI, "base_unit");
    public static final Property dimension = new PropertyImpl(schemaURI, "dimension_type");
    public static final Property applicablePrefixes = new PropertyImpl(schemaURI, "applicable_prefixes");
    public static final Property leftOperand = new PropertyImpl(schemaURI, "left_operand");
    public static final Property rightOperand = new PropertyImpl(schemaURI, "right_operand");
    public static final Property operand = new PropertyImpl(schemaURI, "operand");
    public static final Property power = new PropertyImpl(schemaURI, "power");
}
