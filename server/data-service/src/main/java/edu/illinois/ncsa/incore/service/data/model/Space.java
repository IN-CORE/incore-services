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
    private String id = null;

    private String name = null;
    private List<String> ids = null;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIds() {
        return ids;
    }
    public void setIds(List<String> ids) {this.ids = ids;}

    public void addId(String id) {
        if (id != null) {
            getIds().add(id);
        }
    }
}
