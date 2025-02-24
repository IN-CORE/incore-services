/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado.types;

import dev.morphia.annotations.Embedded;

import java.util.ArrayList;
import java.util.List;

@Embedded
public class EFBox {
    private List<Double> efBoxWidths = new ArrayList<Double>();

    public List<Double> getEfBoxWidths() {
        return efBoxWidths;
    }

    public void setEfBoxWidths(List<Double> efBoxWidths) {
        this.efBoxWidths = efBoxWidths;
    }

}
