package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class Layer {
    private String workspace = "incore";
    private String layerId;
    private String layerType;
    private String datasetCategoryType;
    private String displayName;
    private String description;
    private String styleName;
    private String unit;
    private Boolean visible;

//    TODO: save for later if we have temporal data
//    private String[] timestamps = null;

    public Layer() {}

    public Layer(String layerId, String styleName) {
        this.layerId = layerId;
        this.styleName = styleName;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getLayerType() { return layerType; }

    public void setLayerType(String layerType) { this.layerType = layerType; }

    public String getDatasetCategoryType() { return datasetCategoryType; }

    public void setDatasetCategoryType(String datasetCategoryType){ this.datasetCategoryType = datasetCategoryType; }

    public String getDisplayName(){ return displayName; }

    public void setDisplayName(String displayName){ this.displayName = displayName; }

    public String getDescription(){ return description; }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit(){ return unit; }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public Boolean getVisible(){
        return visible;
    }

    public void setVisible(Boolean visible){
        this.setVisible(visible);
    }

}
