package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;

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

    public Date created = new Date();

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

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getTitle() {
        return title;
    }

    public boolean getIsFinalized() {
        return isFinalized;
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
