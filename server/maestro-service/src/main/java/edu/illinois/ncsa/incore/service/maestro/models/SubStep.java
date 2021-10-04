package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

//    "id": "1-1",
//    "name": "Identify Leader",
//    "description: "Identify resilience leader for the community",
//    "status": ""
//     "required"
//      "required-steps"

//    "users": ["user2", "user3"],
//    "inputs":
//    "hazards":
//    "datasets": ,

@Embedded
public class SubStep {
    public String id;
    public String name;
    public String description;
    public Boolean required;
    public List<String> requiredSteps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public List<String> getRequiredSteps() {
        return requiredSteps;
    }

    public void setRequiredSteps(List<String> requiredSteps) {
        this.requiredSteps = requiredSteps;
    }
}
