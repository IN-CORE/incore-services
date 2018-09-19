package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.complex.Complex;

public class HurricaneSimulation {

    private String absTime;

    private List<Double> gridLats = new ArrayList<>();

    private List<Double> gridLongs = new ArrayList<>();

    private List<List<String>> surfaceVelocity = new ArrayList<>();

    private List<List<Double>> surfaceVelocityAbs = new ArrayList<>();

    public String getAbsTime() {
        return absTime;
    }

    public void setAbsTime(String absTime) {
        this.absTime = absTime;
    }

    public List<Double> getGridLats() {
        return gridLats;
    }

    public void setGridLats(List<Double> gridLats) {
        this.gridLats = gridLats;
    }

    public List<Double> getGridLongs() {
        return gridLongs;
    }

    public void setGridLongs(List<Double> gridLongs) {
        this.gridLongs = gridLongs;
    }

    public List<List<String>> getSurfaceVelocity() {
        return surfaceVelocity;
    }

    public void setSurfaceVelocity(List<List<String>> surfaceVelocity) {
        this.surfaceVelocity = surfaceVelocity;
    }

    public List<List<Double>> getSurfaceVelocityAbs() {
        return surfaceVelocityAbs;
    }

    public void setSurfaceVelocityAbs(List<List<Double>> surfaceVelocityAbs) {
        this.surfaceVelocityAbs = surfaceVelocityAbs;
    }


}
