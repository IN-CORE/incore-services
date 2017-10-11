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
