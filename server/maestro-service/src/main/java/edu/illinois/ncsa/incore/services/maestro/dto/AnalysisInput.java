package edu.illinois.ncsa.incore.services.maestro.dto;

public class AnalysisInput {

    private String name;
    private String description;
    private Boolean required;
    private Boolean advanced;
    private Boolean multiple;
    private String type;

    public AnalysisInput() {}

    public AnalysisInput(String name, String description, String type, Boolean required, Boolean advanced, Boolean multiple){
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.advanced = advanced;
        this.multiple = multiple;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getRequired() {
        return required;
    }

    public Boolean getAdvanced() {
        return advanced;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public String getType() {
        return type;
    }
}
