package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tornadoType")
@JsonSubTypes({@JsonSubTypes.Type(value = TornadoDataset.class, name = "dataset"), @JsonSubTypes.Type(value = TornadoModel.class, name = "model")})
@XmlRootElement
public class Tornado {
    @Id
    @Property("_id")
    private ObjectId id;
    // Friendly name of Tornado hazard
    private String name;
    private String description;

    private Privileges privileges;
    private Date date = new Date();

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

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Date created
     *
     * @return date created
     */
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
