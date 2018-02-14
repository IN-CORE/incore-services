/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado.types;

import java.util.ArrayList;
import java.util.List;

public class EFBox {
    private List<Double> efBoxWidths = new ArrayList<Double>();

    public List<Double> getEfBoxWidths() {
        return efBoxWidths;
    }

    public void setEfBoxWidths(List<Double> efBoxWidths) {
        this.efBoxWidths = efBoxWidths;
    }

}
