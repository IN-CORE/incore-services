package edu.illinois.ncsa.incore.service.project.models;

import java.util.Date;
import java.util.List;

public class WorkflowResource extends ProjectResource {

    // Enum for status
    public enum Type {
        workflow,
        execution
    }
    public Type type;

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
