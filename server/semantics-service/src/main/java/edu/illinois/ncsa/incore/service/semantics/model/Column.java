package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

@Embedded
public class Column {

    private String name;
    private String titles;
    private String dataType;

    @Property("dc:description")
    private String description;

    @Property("qudt:unit")
    private String unit;

    private Boolean required;

    public Column() {
    }

    public Column(String name, String titles, String dataType, String description, String unit, Boolean required) {
        this.name = name;
        this.titles = titles;
        this.dataType = dataType;
        this.description = description;
        this.unit = unit;
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public String getTitles() {
        return this.titles;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() { return description; }

    public Boolean getRequired(){ return required; }
}
