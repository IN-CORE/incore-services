package edu.illinois.ncsa.incore.service.data.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by ywkim on 10/2/2017.
 */

@XmlRootElement
public class Space {
    @Id
    @Property("_id")
    private ObjectId id = new ObjectId();

    private String name = null;
    private List<String> datasetIds = null;

    public String getId() {
        return id.toString();
    }
    public void setId(String id) {
        this.id = new ObjectId(id);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDatasetIds() {
        return datasetIds;
    }
    public void setDatasetIds(List<String> datasetIds) {this.datasetIds = datasetIds;}

    public void addId(String id) {
        if (id != null) {
            getDatasetIds().add(id);
        }
    }
}
