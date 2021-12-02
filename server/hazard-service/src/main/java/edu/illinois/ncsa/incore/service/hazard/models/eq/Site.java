/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import org.locationtech.jts.geom.Point;

public class Site {
    private Point location;
    private double depth = 0.0;
    private double vs30 = 760.0;
    private String siteClass;

    public Site() {
    }

    public void setSiteClass(String siteClass) {
        this.siteClass = siteClass;
    }

    public String getSiteClass() {
        return this.siteClass;
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
