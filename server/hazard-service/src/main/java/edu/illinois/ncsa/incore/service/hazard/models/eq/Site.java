/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import com.vividsolutions.jts.geom.Point;

public class Site {
    private Point location;
    private double depth = 0.0;
    private double vs30 = 760.0;

    public Site() {
    }

    public Site(Point location) {
        this.location = location;
    }

    public Site(Point location, double depth) {
        this.location = location;
        this.depth = depth;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getVs30() {
        return vs30;
    }

    public void setVs30(double vs30) {
        this.vs30 = vs30;
    }
}
