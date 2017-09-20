package edu.illinois.ncsa.incore.services.maestro.dto;

public class AnalysisOutput {

    private String name;
    private String type;
    private String description;

    public AnalysisOutput(){}

    public AnalysisOutput(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
