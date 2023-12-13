package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

@Embedded
public class Column {

    private String name;
    private String titles;
    private String datatype;

    @Property("dc:description")
    private String description;

    @Property("qudt:unit")
    private String unit;

    private String required;

    public Column() {
    }

    public Column(String name, String titles, String datatype, String description, String unit, String required) {
        this.name = name;
        this.titles = titles;
        this.datatype = datatype;
        this.description = description;
        this.unit = unit;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getTitles() {
        return titles;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() { return description; }

    public String getRequired(){ return required; }
}
