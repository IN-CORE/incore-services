package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetResource extends ProjectResource{

    // TODO Read from dataset object
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)

    public boolean deleted = false;
    public String title;
    public String description = "";
    public String creator = null;
    public String owner = null;
    public List<String> contributors = null;
    public List<FileDescriptor> fileDescriptors = null;

    private String type;
    private String dataType;

    public String storedUrl = "";
    public String format = "";
    public String sourceDataset = "";
    public double[] boundingBox = null;
    public NetworkDataset networkDataset = null;
    public WorkflowMetadata[] workflowMetadata = null;

    public DatasetResource() {
    }

    // Getter for type with fallback to dataType if type is not set
    public String getType() {
        if (type != null && !type.isEmpty()) {
            return type;
        }
        return dataType;  // Fallback to dataType if type is null or empty
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    // Setter for type
    public void setType(String type) {
        this.type = type;
    }

    // Getter and setter for dataType
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.title != null && this.title.toLowerCase().contains(lowerCaseText)) ||
            (this.description != null && this.description.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.toLowerCase().contains(lowerCaseText)) ||
            (this.owner != null && this.owner.toLowerCase().contains(lowerCaseText)) ||
            (this.contributors != null && this.contributors.stream().anyMatch(c -> c.toLowerCase().contains(lowerCaseText))) ||
            (this.type != null && this.getType().toLowerCase().contains(lowerCaseText)) ||
            (this.dataType != null && this.getDataType().toLowerCase().contains(lowerCaseText)) ||
            (this.format != null && this.format.toLowerCase().contains(lowerCaseText));
    }

    public boolean hasWorkflowId(DatasetResource dataset, String workflowId) {
        if (dataset.workflowMetadata == null) return false;
        return Arrays.stream(dataset.workflowMetadata)
            .anyMatch(meta -> workflowId.equals(meta.getWorkflowId()));
    }

    public boolean hasExecutionId(DatasetResource dataset, String executionId) {
        if (dataset.workflowMetadata == null) return false;
        return Arrays.stream(dataset.workflowMetadata)
            .anyMatch(meta -> executionId.equals(meta.getExecutionId()));
    }
}

