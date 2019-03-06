/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io;

public enum RDFFormat {
    TURTLE("TURTLE"),
    NTRIPLES("N-TRIPLES"),
    JSONLD("JSON-LD"),
    RDFXMLABBREV("RDF/XML-ABBREV"),
    RDFXML("RDF/XML"),
    N3("N3"),
    RDFJSON("RDF/JSON");

    private String value;

    private RDFFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
