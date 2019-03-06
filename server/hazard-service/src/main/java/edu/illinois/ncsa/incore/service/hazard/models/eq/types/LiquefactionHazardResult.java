/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LiquefactionHazardResult {
    private double latitude;
    private double longitude;
    private String pgdUnits;
    private double pgd;
    private double liqProbability;
    private double[] groundFailureProb;

    public LiquefactionHazardResult(double latitude, double longitude, double pgd, String pgdUnits, double liqProbability, double[] groundFailureProb) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.pgd = pgd;
        this.pgdUnits = pgdUnits;
        this.liqProbability = liqProbability;
        this.groundFailureProb = groundFailureProb;
    }

    public String getPgdUnits() {
        return pgdUnits;
    }

    public void setPgdUnits(String pgdUnits) {
        this.pgdUnits = pgdUnits;
    }

    public double getPgd() {
        return pgd;
    }

    public void setPgd(double pgd) {
        this.pgd = pgd;
    }

    public double getLiqProbability() {
        return liqProbability;
    }

    public void setLiqProbability(double liqProbability) {
        this.liqProbability = liqProbability;
    }

    public double[] getGroundFailureProb() {
        return groundFailureProb;
    }

    public void setGroundFailureProb(double[] groundFailureProb) {
        this.groundFailureProb = groundFailureProb;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
