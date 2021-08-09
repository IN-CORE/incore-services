package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IncorePoint {
    private Point location;

    public IncorePoint(String latlong) {
        String[] str = latlong.split(",");
        if (str.length != 2) {
            throw new IllegalArgumentException("The format of the location needs to be decimalLat,decimalLong");
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
        return this.location.getY() + ", " + this.location.getX();
    }
}
