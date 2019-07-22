/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.resilience.models;

public class PaperReference {
    public String name;
    public String doi;
    public String yearPublished;

    public PaperReference() {}

    public PaperReference(String name, String yearPublished, String doi) {
        this.name = name;
        this.doi = doi;
        this.yearPublished = yearPublished;
    }

    public PaperReference(String name) {
        this.name = name;
    }

    public PaperReference(String name, String yearPublished) {
        this.name = name;
        this.yearPublished = yearPublished;
    }
}
