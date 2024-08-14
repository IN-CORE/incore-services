package edu.illinois.ncsa.incore.service.project.models;

public class ProjectResource {

    // Enum for status
    public enum Status {
        DELETED,
        UNAUTHORIZED,
        EXISTING,
    }

    private String id;
    private Status status;

    public ProjectResource(String id, Status status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

