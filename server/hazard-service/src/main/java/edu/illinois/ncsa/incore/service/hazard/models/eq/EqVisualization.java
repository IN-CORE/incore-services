/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

public class EqVisualization {
    private String demandType;
    private String demandUnits;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private int numPoints;
    private boolean amplifyHazard;

    public EqVisualization() {

    }

    public String getDemandType() {
        return demandType;
    }

    public void setDemandType(String demandType) {
        this.demandType = demandType;
    }

    public String getDemandUnits() {
        return demandUnits;
    }

    public void setDemandUnits(String demandUnits) {
        this.demandUnits = demandUnits;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public boolean isAmplifyHazard() {
        return amplifyHazard;
    }

    public void setAmplifyHazard(boolean amplifyHazard) {
        this.amplifyHazard = amplifyHazard;
    }

    public int getNumPoints() {
        return numPoints;
    }

    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }
}
