package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

@Entity("Type")
public class Type {
    @Id
    @Property("_id")
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("dc:title")
    private String titles;
    private String datatype;

    @Property("dc:description")
    private String description;

    @Property("qudt:unit")
    private String unit;

    @Property("openvocab:versionnumber")
    private String version;

    private String url;

    private String required;

    public Type() {
    }

    public Type(String name, String titles, String datatype, String description, String unit, String required) {
        this.name = name;
        this.titles = titles;
        this.datatype = datatype;
        this.description = description;
        this.unit = unit;
        this.required = required;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getName() {
        return this.name;
    }

    public String getTitles() {
        return this.titles;
    }

    public String getDescription() { return description; }

    public String getVersion() { return version; }

    public String getUrl() { return url; }
}
