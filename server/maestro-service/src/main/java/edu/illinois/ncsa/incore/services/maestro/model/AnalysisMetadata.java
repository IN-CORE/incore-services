package edu.illinois.ncsa.incore.services.maestro.model;

import edu.illinois.ncsa.incore.services.maestro.dto.AnalysisInput;
import edu.illinois.ncsa.incore.services.maestro.dto.AnalysisOutput;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AnalysisMetadata {
    @Property("_id")
    private String id;

    private String description;
    private String name;
    private String url;
    private String category;
    private String helpContext;

    public AnalysisMetadata() {}

    public AnalysisMetadata(String id, String name, String description, String category, String url, String helpContext){
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.category = category;
        this.helpContext = helpContext;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }

    public String getHelpContext() {
        return helpContext;
    }
}
