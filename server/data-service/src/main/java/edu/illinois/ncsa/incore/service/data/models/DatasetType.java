package edu.illinois.ncsa.incore.service.data.models;

import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatasetType {

    /**
     * Unique identifier for this bean, used by persistence layer
     */
    @Id
    @Property("_id")
    private ObjectId id = new ObjectId();

    private String dataType;

    private String space;

    public String getSpace() {
        int idxColon = dataType.indexOf(":");
        if (idxColon >= 0) {
            return dataType.substring(0, dataType.indexOf(":"));
        }
        return "";
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
