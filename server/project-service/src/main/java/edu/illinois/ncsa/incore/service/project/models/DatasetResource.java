package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;

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

    public DatasetResource() {
    }

    // Getter for type with fallback to dataType if type is not set
    public String getType() {
        if (type != null && !type.isEmpty()) {
            return type;
        }
        return dataType;  // Fallback to dataType if type is null or empty
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
}

