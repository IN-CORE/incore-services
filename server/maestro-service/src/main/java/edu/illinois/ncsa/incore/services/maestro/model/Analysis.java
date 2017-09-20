package edu.illinois.ncsa.incore.services.maestro.model;

import edu.illinois.ncsa.incore.services.maestro.dto.AnalysisInput;
import edu.illinois.ncsa.incore.services.maestro.dto.AnalysisOutput;
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
    private List<AnalysisInput> inputs;
    private List<AnalysisOutput> outputs;

    public Analysis() {}

    public Analysis(String name, String description, String category, String url, List<AnalysisInput> inputs,
                    List<AnalysisOutput> outputs ){
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

    public List<AnalysisInput> getInputs() {
        return inputs;
    }

    public List<AnalysisOutput> getOutputs() {
        return outputs;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
