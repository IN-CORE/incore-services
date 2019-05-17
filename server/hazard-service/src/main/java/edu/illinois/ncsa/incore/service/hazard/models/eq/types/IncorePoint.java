/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Gowtham Naraharisetty
 * Chris Navarro
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.Point;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IncorePoint {
    private Point location;

    public IncorePoint(String latlong) {
        String[] str = latlong.split(",");
        if (str.length != 2) {
            throw new IllegalArgumentException("The format needs to be decimalLat,decimalLong");
        }
        double latitude = Double.parseDouble(str[0]);
        double longitude = Double.parseDouble(str[1]);

        GeometryFactory factory = new GeometryFactory();
        location = factory.createPoint(new Coordinate(longitude, latitude));
    }

    public Point getLocation() {
        return location;
    }

    public IncorePoint fromString(final String latlong) {
        return new IncorePoint(latlong);
    }

    public String toString() {
        return  this.location.getY()+", "+this.location.getX();
    }
}
