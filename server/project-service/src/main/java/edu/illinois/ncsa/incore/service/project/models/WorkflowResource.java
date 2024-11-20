package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    public Type type = Type.workflow; // default to workflow
    public boolean isFinalized = false;

    // only keep basic field
    public boolean deleted;
    public String title;
    public String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    public Date created;

    public WorkflowCreator creator;
    public List<String> contributors;

    public WorkflowResource() {
    }

    // Getter and Setter for type
    public Type getType() {
        return type;
    }

    public void setType(WorkflowResource.Type type) {
        this.type = type;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Search functionality
    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.title != null && this.title.toLowerCase().contains(lowerCaseText)) ||
            (this.description != null && this.description.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.lastName.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.firstName.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.email.toLowerCase().contains(lowerCaseText)) ||
            (this.contributors != null && this.contributors.stream().anyMatch(c -> c.toLowerCase().contains(lowerCaseText))) ||
            (this.type != null && this.getType().toString().toLowerCase().contains(lowerCaseText));
    }
}
