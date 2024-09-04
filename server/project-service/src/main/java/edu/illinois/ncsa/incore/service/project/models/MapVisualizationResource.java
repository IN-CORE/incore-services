package edu.illinois.ncsa.incore.service.project.models;

import java.util.List;

public class MapVisualizationResource extends VisualizationResource{
    private double[] boundingBox = null;
    private List<Layer> layers;

    public MapVisualizationResource() {
        super(Type.MAP);
    }

    public MapVisualizationResource(Type type) {
        super(type);
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
        this.boundingBox = boundingBox;
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
