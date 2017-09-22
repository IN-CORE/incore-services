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
    private String helpContext;
    private String tag;
    private List<AnalysisInput> inputs;
    private List<AnalysisOutput> outputs;
    private AnalysisMetadata metadata;

    public Analysis() {}

    public Analysis(String name, String description, String category, String url, List<AnalysisInput> inputs,
                    List<AnalysisOutput> outputs, String tag, String helpContext){
        this.name = name;
        this.description = description;
        this.url = url;
        this.inputs = inputs;
        this.outputs = outputs;
        this.category = category;
        this.tag = tag;
        this.helpContext = helpContext;
    }

    public AnalysisMetadata getMetadata() {
        if(metadata == null) {
            metadata = new AnalysisMetadata(this.id, this.name, this.description, this.category, this.url, this.helpContext);
        }
        return metadata;
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

    public String getHelpContext() {
        return helpContext;
    }

    public String getTag() {
        return tag;
    }
}
