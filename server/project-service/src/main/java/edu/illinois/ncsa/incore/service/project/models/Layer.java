package edu.illinois.ncsa.incore.service.project.models;

class Layer {
    private String workspace = "incore";
    private String layerId;
    private String styleName;

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

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }
}
