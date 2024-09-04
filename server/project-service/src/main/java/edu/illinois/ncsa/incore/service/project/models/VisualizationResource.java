package edu.illinois.ncsa.incore.service.project.models;

import java.util.List;

public class VisualizationResource extends ProjectResource{

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

