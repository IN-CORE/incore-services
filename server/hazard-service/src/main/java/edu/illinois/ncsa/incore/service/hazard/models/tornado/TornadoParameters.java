/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import java.util.ArrayList;
import java.util.List;

public class TornadoParameters {
    private String efRating;
    private double maxWindSpeed = 250.0;
    private double startLatitude;
    private double startLongitude;
    private int randomSeed;
    private int windSpeedMethod = 1;
    private int numSimulations = 1;
    private List<Double> endLatitude = new ArrayList<Double>();
    private List<Double> endLongitude = new ArrayList<Double>();

    public int getWindSpeedMethod() {
        return windSpeedMethod;
    }

    public void setWindSpeedMethod(int windSpeedMethod) {
        this.windSpeedMethod = windSpeedMethod;
    }

    public List<Double> getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(List<Double> endLatitude) {
        this.endLatitude = endLatitude;
    }

    public List<Double> getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(List<Double> endLongitude) {
        this.endLongitude = endLongitude;
    }


    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }

    public String getEfRating() {
        return efRating;
    }

    public void setEfRating(String efRating) {
        this.efRating = efRating;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public int getNumSimulations(){ return numSimulations; }

    public void setNumSimulations(int numSimulations){ this.numSimulations = numSimulations; }

}
