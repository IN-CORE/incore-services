package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowResource extends ProjectResource {

    // Enum for status
    public enum Type {
        workflow,
        execution
    }

    public Type type;
    public boolean isFinalized = false;

    // only keep basic field
    public boolean deleted;
    public String title;
    public String description;
    public Date created;
    public WorkflowCreator creator;
    public List<String> contributors;

    public WorkflowResource() {
    }

    // Getter and Setter for hazardType
    public Type getType() {
        return type;
    }

    public void setType(WorkflowResource.Type type) {
        this.type = type;
    }

}
