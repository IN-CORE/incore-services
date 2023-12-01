package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.List;

@Entity("Type")
public class Type {
    @Id
    @Property("_id")
    private ObjectId id;

    @Property("dc:title")
    private String title;

    private String url;

    private Columns tableSchema;

    @Property("openvocab:versionnumber")
    private String version;

    @Property("dc:description")
    private String description;

    // TODO how to represent @context
    // @Embedded("@context")
    // private List<Context> context;

    public Type() {
    }

    public Type(String url, String title, Columns tableSchema, List<Context> context) {
        this.url = url;
        this.title = title;
        this.tableSchema = tableSchema;
        // this.context = context;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Columns getTableSchema() {
        return tableSchema;
    }

    public String getVersion() { return version; }

    public String getDescription() { return description; }

    // public List<Context> getContext() {
    // return context;
    // }
}
