package edu.illinois.ncsa.incore.service.semantic.model;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Embedded;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
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
