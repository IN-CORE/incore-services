package edu.illinois.ncsa.incore.service.project.models;

import java.util.List;

public abstract class VisualizationResource {

    // Enum for visualization types
    public enum Type {
        MAP,
        CHART,
        TABLE
    }

    private Type type;

    public VisualizationResource(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

// Subclass for Map visualizations
class MapVisualization extends VisualizationResource {
    private List<Layer> layers;

    public MapVisualization() {
        super(Type.MAP);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
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
}

// Subclass for Chart visualizations
class ChartVisualization extends VisualizationResource {
    private String vegaJson;
    private List<String> sourceDatasetIds;

    public ChartVisualization() {
        super(Type.CHART);
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
}

// Subclass for Table visualizations
class TableVisualization extends VisualizationResource {
    public TableVisualization() {
        super(Type.TABLE);
    }
}

