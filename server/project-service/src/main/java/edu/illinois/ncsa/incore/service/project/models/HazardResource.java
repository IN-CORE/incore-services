package edu.illinois.ncsa.incore.service.project.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;

import java.util.Date;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class HazardResource extends ProjectResource {

    // Enum for status
    public enum Type {
        earthquake,
        tornado,
        tsunami,
        flood,
        hurricane,
    }

    public Type type = Type.earthquake;

    // Only keep basic information for now
    public String name = null;
    public String description;
    public Date date = new Date();
    public String creator = null;
    public String owner = null;
    public List<HazardDataset> hazardDatasets;

    public HazardResource() {
    }

    // Getter and Setter for hazardType
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }

    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.name != null && this.name.toLowerCase().contains(lowerCaseText)) ||
            (this.description != null && this.description.toLowerCase().contains(lowerCaseText)) ||
            (this.creator != null && this.creator.toLowerCase().contains(lowerCaseText)) ||
            (this.owner != null && this.owner.toLowerCase().contains(lowerCaseText)) ||
            (this.type != null && this.getType().toString().toLowerCase().contains(lowerCaseText));
    }
}
