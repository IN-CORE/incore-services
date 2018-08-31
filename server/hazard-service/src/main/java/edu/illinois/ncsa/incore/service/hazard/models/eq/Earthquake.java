package edu.illinois.ncsa.incore.service.hazard.models.eq;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eqType")
@JsonSubTypes({@JsonSubTypes.Type(value = EarthquakeDataset.class, name = "dataset"), @JsonSubTypes.Type(value = EarthquakeModel.class, name = "model")})
@XmlRootElement
public abstract class Earthquake {
    @Id
    @Property("_id")
    private ObjectId id;
    private Privileges privileges;

    public void setName(String name) {
        this.name = name;
    }

    // Friendly name of defined earthquake
    private String name;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id.toString();
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public String getName() {
        return this.name;
    }
}
