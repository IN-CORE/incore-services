package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetResource extends ProjectResource{

    // TODO Read from dataset object
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)

    public String type;
    public boolean deleted = false;
    public String title = "";
    public String description = "";
    public Date date = new Date();
    public String creator = null;
    public String owner = null;
    public List<String> contributors = null;
    public List<FileDescriptor> fileDescriptors = null;
    public String dataType = "";
    public String storedUrl = "";
    public String format = "";
    public String sourceDataset = "";
    public double[] boundingBox = null;
    public List<String> spaces = null;
    public NetworkDataset networkDataset = null;

    public DatasetResource() {
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }
}
