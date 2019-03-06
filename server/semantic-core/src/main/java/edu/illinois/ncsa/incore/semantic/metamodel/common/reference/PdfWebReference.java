/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.common.reference;

public class PdfWebReference extends WebReference {
    public int pageNumber;

    public PdfWebReference(String url, int pageNumber) {
        super(url);
        this.pageNumber = pageNumber;
    }

    @Override
    public String getUrl() {
        return url + "#page=" + pageNumber;
    }
}
