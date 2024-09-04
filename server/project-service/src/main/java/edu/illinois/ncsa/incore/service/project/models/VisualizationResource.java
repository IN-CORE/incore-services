package edu.illinois.ncsa.incore.service.project.models;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MapVisualizationResource.class, name = "MAP"),
    @JsonSubTypes.Type(value = ChartVisualizationResource.class, name = "CHART"),
    @JsonSubTypes.Type(value = TableVisualizationResource.class, name = "TABLE")
})
public class VisualizationResource extends ProjectResource{

    // Enum for visualization types
    public enum Type {
        MAP,
        CHART,
        TABLE
    }

    private Type type;

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
}

