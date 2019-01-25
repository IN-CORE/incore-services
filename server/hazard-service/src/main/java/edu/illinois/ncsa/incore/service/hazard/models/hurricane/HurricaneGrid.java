package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;

import java.util.ArrayList;
import java.util.List;

public class HurricaneGrid {

    private List<Double> xs = new ArrayList<Double>();
    private List<Double> ys = new ArrayList<Double>();
    private List<Double> latis = new ArrayList<Double>();
    private List<Double> longis = new ArrayList<Double>();
    private List<Double> lati = new ArrayList<Double>();
    private List<Double> longi = new ArrayList<Double>();
    private IncorePoint center = new IncorePoint("0,0");

    public IncorePoint getCenter() {
        return center;
    }

    public void setCenter(IncorePoint center) {
        this.center = center;
    }

    public List<Double> getXs() {
        return xs;
    }

    public void setXs(List<Double> xs) {
        this.xs = xs;
    }

    public List<Double> getYs() {
        return ys;
    }

    public void setYs(List<Double> ys) {
        this.ys = ys;
    }

    public List<Double> getLatis() {
        return latis;
    }

    public void setLatis(List<Double> latis) {
        this.latis = latis;
    }

    public List<Double> getLongis() {
        return longis;
    }

    public void setLongis(List<Double> longis) {
        this.longis = longis;
    }

    public List<Double> getLati() {
        return lati;
    }

    public void setLati(List<Double> lati) {
        this.lati = lati;
    }

    public List<Double> getLongi() {
        return longi;
    }

    public void setLongi(List<Double> longi) {
        this.longi = longi;
    }


}
