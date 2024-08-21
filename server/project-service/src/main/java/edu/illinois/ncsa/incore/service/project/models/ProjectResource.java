package edu.illinois.ncsa.incore.service.project.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class ProjectResource {

    // Enum for status
    public enum Status {
        DELETED,
        UNAUTHORIZED,
        EXISTING,
    }

    private String id;

    //TODO: Live calculate the status
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Status status=Status.EXISTING;

    public ProjectResource() {
    }

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

