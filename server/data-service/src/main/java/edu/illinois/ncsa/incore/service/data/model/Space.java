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
    private ObjectId spaceId;

    private String name;
    private List<ObjectId> datasetIds;

    public ObjectId getSpaceId() {
        return spaceId;
    }
    public void setSpaceId(ObjectId spaceId) {
        this.spaceId = spaceId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<ObjectId> getDatasetIds() {
        return datasetIds;
    }
    public void addDatasetId(ObjectId datasetId) {
        if (datasetId != null) {
            getDatasetIds().add(datasetId);
        }
    }
}
