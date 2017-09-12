package edu.illinois.ncsa.incore.services.maestro.model;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
public class Analysis {

    @Id
    @Property("_id")
    private String id;

    private String description;
    private String name;
    private String url;
    private String category;
    private List<String> inputs;
    private List<String> outputs;

    public Analysis() {}

    public Analysis(String name, String description, String category, String url, List<String> inputs, List <String> outputs ){
        this.name = name;
        this.description = description;
        this.url = url;
        this.inputs = inputs;
        this.outputs = outputs;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
