package edu.illinois.ncsa.incore.service.data.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity(value = "DatasetType", useDiscriminator = false)
public class DatasetType {

    /**
     * Unique identifier for this bean, used by persistence layer
     */
    @Id
    @Property("_id")
    private final ObjectId id = new ObjectId();

    private String dataType;

    private String space;

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
