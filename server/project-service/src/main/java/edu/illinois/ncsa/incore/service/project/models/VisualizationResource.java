package edu.illinois.ncsa.incore.service.project.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualizationResource extends ProjectResource{

    // Enum for visualization types
    public enum Type {
        MAP,
        CHART,
        TABLE
    }

    private Type type = Type.MAP;
    private double[] boundingBox = null;
    private List<String> layerOrder = new ArrayList<>();
    private List<Layer> layers = new ArrayList<>();
    private String vegaJson = null;
    public String name;
    public String description;

    public VisualizationResource() {}
    public VisualizationResource(Type type) {
        this.type = type;
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
        if (layers == null) {
            layers = new ArrayList<>();
        }

        // Check if the layer already exists in the list
        boolean exists = layers.stream()
            .anyMatch(existingLayer -> existingLayer.getLayerId().equals(layer.getLayerId()));

        if (!exists) {
            layers.add(layer);

            if (layerOrder == null) {
                layerOrder = new ArrayList<>();
            }

            // Add layerId to layerOrder if not already present
            if (!layerOrder.contains(layer.getLayerId())) {
                layerOrder.add(layer.getLayerId());
            }
        }
    }

    public void updateLayer(Layer layer) {
        boolean updated = false;
        // if it exist, replace the existing layer with new one same order
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).getLayerId().equals(layer.getLayerId())) {
                layers.set(i, layer);
                updated = true;
                break;
            }
        }
       if (!updated) {
            throw new IllegalArgumentException("Layer with ID " + layer.getLayerId() + " does not exist.");
        }
    }

    public void removeLayerById(String layerId) {
        int indexToRemove = -1;
        for (int i = 0; i < layers.size(); i++) {
            if (layerId.equals(layers.get(i).getLayerId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            layers.remove(indexToRemove);
        }

        if (layerOrder != null) {
            layerOrder.remove(layerId);
        }
    }

    public void syncLayerOrder() {
        if (layerOrder == null) {
            layerOrder = new ArrayList<>();
        }

        Set<String> validLayerIds = layers.stream()
            .map(Layer::getLayerId)
            .collect(Collectors.toSet());

        // Step 1: Remove stale IDs
        layerOrder.removeIf(id -> !validLayerIds.contains(id));

        // Step 2: Add missing valid layer IDs (preserve order in `layers`)
        for (Layer layer : layers) {
            String id = layer.getLayerId();
            if (!layerOrder.contains(id)) {
                layerOrder.add(id);
            }
        }
    }

    public String getVegaJson() {
        return vegaJson;
    }

    public void setVegaJson(String vegaJson) {
        this.vegaJson = vegaJson;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getLayerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(List<String> layerOrder) {
        this.layerOrder = layerOrder;
    }

    public boolean matchesSearchText(String text) {
        String lowerCaseText = text.toLowerCase();
        return (this.getId() != null && this.getId().equals(lowerCaseText)) ||
            (this.name != null && this.name.toLowerCase().contains(lowerCaseText)) ||
            (this.description != null && this.description.toLowerCase().contains(lowerCaseText)) ||
            (this.vegaJson != null && this.vegaJson.toLowerCase().contains(lowerCaseText)) ||
            (this.type != null && this.getType().toString().toLowerCase().contains(lowerCaseText));
    }
}

