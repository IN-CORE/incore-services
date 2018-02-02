package edu.illinois.ncsa.incore.service.hazard.models.eq.types;

public class HazardResult {

    private double latitude;
    private double longitude;
    private double hazardValue;

    public HazardResult(double latitude, double longitude, double hazardValue) {
       this.latitude = latitude;
       this.longitude = longitude;
       this.hazardValue = hazardValue;
    }
    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }
    public double getHazardValue() {
        return hazardValue;
    }
}
