package edu.illinois.ncsa.incore.service.project.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualizationResource extends ProjectResource{

    // Enum for visualization types
    public enum Type {
        MAP,
        CHART,
        TABLE
    }

    private Type type;
    private double[] boundingBox = null;
    private List<Layer> layers = null;
    private String vegaJson = null;
    private List<String> sourceDatasetIds = null;
    public String name;
    public String description;

    public VisualizationResource() {}
    public VisualizationResource(Type type) {
        this.type = type;
    }
    public Date date = new Date();

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public double[] getBoundingBox() {
        return boundingBox;
    }
    public void setBoundingBox(double[] boundingBox) {
        // TODO: add logic to set boundingbox using the first layer if not given
        this.boundingBox = boundingBox;
    }

    public void addLayer(Layer layer) {
        // Check if the layer already exists in the list
        boolean exists = layers.stream()
            .anyMatch(existingLayer -> existingLayer.getLayerId().equals(layer.getLayerId()));

        if (!exists) {
            layers.add(layer);
        }
    }

    public void removeLayer(Layer layer) {
        int indexToRemove = -1;
        for (int i = 0; i < layers.size(); i++) {
            if (layer.getLayerId().equals(layers.get(i).getLayerId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            layers.remove(indexToRemove);
        }
    }

    public String getVegaJson() {
        return vegaJson;
    }

    public void setVegaJson(String vegaJson) {
        this.vegaJson = vegaJson;
    }

    public  List<String> getDatasetIds() {
        return sourceDatasetIds;
    }

    public void setDatasetId(List<String> sourceDatasetIds) {
        this.sourceDatasetIds = sourceDatasetIds;
    }

    public void addSourceDatasetId(String id) {
        sourceDatasetIds.add(id);
    }

    public void removeSourceDatasetId(String id) {
        sourceDatasetIds.remove(id);
    }

    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.vegaJson != null && this.vegaJson.toLowerCase().contains(lowerCaseText)) ||
            (this.type != null && this.getType().toString().toLowerCase().contains(lowerCaseText));
    }
}

