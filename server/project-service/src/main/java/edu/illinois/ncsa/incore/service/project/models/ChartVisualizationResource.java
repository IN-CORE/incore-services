package edu.illinois.ncsa.incore.service.project.models;

import java.util.List;

public class ChartVisualizationResource extends VisualizationResource{
    private String vegaJson;
    private List<String> sourceDatasetIds;

    public ChartVisualizationResource() {
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
