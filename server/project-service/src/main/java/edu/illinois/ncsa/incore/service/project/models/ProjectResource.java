package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class ProjectResource {

    // Enum for status
    public enum Status {
        DELETED,
        UNAUTHORIZED,
        EXISTING,
        UNKNOWN
    }
    private String id;

    // Live calculate the status
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Status status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    public ProjectResource() {
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

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }
}

