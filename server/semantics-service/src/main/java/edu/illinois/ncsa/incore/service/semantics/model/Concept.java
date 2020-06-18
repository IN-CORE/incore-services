package edu.illinois.ncsa.incore.service.semantics.model;

import org.bson.types.ObjectId;
import dev.morphia.morphia.annotations.Id;
import dev.morphia.morphia.annotations.Entity;
import dev.morphia.morphia.annotations.Property;
import dev.morphia.morphia.annotations.Embedded;

import java.util.List;

@Entity("Concept")
public class Concept {
    @Id
    private ObjectId id;

    @Property("dc:title")
    private String title;

    private String url;

    @Embedded
    private Columns tableSchema;

    // TODO how to represent @context
    // @Embedded("@context")
    // private List<Context> context;

    public Concept() { }

    public Concept(String url, String title, Columns tableSchema, List<Context> context) {
        this.url = url;
        this.title = title;
        this.tableSchema = tableSchema;
        // this.context = context;
    }

    public String getId() {
        return id.toHexString();
    }

    public String getUrl(){
        return url;
    }

    public String getTitle(){
        return title;
    }

    public Columns getTableSchema(){
        return tableSchema;
    }

    // public List<Context> getContext() {
    // return context;
    // }
}
