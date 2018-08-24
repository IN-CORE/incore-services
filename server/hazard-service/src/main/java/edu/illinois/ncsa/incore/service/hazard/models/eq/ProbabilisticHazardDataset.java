package edu.illinois.ncsa.incore.service.hazard.models.eq;

public class ProbabilisticHazardDataset extends HazardDataset {

    // e.g. 50 years, or 1 in 50, or 2% probability in any given year
    private int recurrenceInterval;
    // e.g. years
    private String recurrenceUnit;

    public ProbabilisticHazardDataset() {

    }

    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(int recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }

    public String getRecurrenceUnit() {
        return recurrenceUnit;
    }

    public void setRecurrenceUnit(String recurrenceUnit) {
        this.recurrenceUnit = recurrenceUnit;
    }

}
