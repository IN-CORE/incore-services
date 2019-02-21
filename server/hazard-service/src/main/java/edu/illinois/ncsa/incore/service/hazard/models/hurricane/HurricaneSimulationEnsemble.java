/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import java.util.ArrayList;
import java.util.List;

public class HurricaneSimulationEnsemble {

    public final String resolutionUnits = "km";
    public final String transDUnits = "degree";
    public final String velocityUnits = "kt";
    public final String gridRowType = "latitude";
    public final String gridColumnType = "longitude";
    public final String description = "Surface Windfield is simulated for the provided times. A simulated grid that " +
        "represents the affected area for each time is also returned ";
    public List<HurricaneSimulation> hurricaneSimulations = new ArrayList();
    private int resolution;
    private double transD;
    private String landfallLocation;
    private String modelUsed;
    private List<String> times = new ArrayList();
    private List<String> centers = new ArrayList();
    private List<String> centerVelocities = new ArrayList();

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public double getTransD() {
        return transD;
    }

    public void setTransD(double transD) {
        this.transD = transD;
    }

    public String getLandfallLocation() {
        return landfallLocation;
    }

    public void setLandfallLocation(String landfallLocation) {
        this.landfallLocation = landfallLocation;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public List<HurricaneSimulation> getHurricaneSimulations() {
        return hurricaneSimulations;
    }

    public void setHurricaneSimulations(List<HurricaneSimulation> hurricaneSimulations) {
        this.hurricaneSimulations = hurricaneSimulations;
    }

    public List<String> getCenters() {
        return centers;
    }

    public void setCenters(List<String> centers) {
        this.centers = centers;
    }

    public List<String> getCenterVelocities() {
        return centerVelocities;
    }

    public void setCenterVelocities(List<String> centerVelocities) {
        this.centerVelocities = centerVelocities;
    }
}
