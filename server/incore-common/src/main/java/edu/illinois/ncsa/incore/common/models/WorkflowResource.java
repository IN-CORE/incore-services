package edu.illinois.ncsa.incore.common.models;

public class WorkflowResource extends ProjectResource {

    // Enum for status
    private enum Type {
        workflow,
        execution
    }
    private WorkflowResource.Type type;

    public WorkflowResource(String id, Status status, WorkflowResource.Type type) {
        super(id, status);
        this.type = type;
    }

    // Getter and Setter for hazardType
    public WorkflowResource.Type getType() {
        return type;
    }

    public void setHazardType(WorkflowResource.Type type) {
        this.type = type;
    }
}
