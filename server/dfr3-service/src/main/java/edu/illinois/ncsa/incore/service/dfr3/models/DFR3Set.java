package edu.illinois.ncsa.incore.service.dfr3.models;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
public abstract class DFR3Set {
    @Id
    protected ObjectId id;

    @XmlTransient
    protected String legacyId;

    protected String description;

    protected List<String> authors = new ArrayList<>();
    protected PaperReference paperReference;

    protected String resultUnit;
    protected String resultType;


    protected String hazardType;
    protected String inventoryType;

    protected Privileges privileges;
    protected String creator;

    // region Getters
    public String getId() {
        if (id == null) {
            return null;
        } else {
            return id.toHexString();
        }
    }

    public String getLegacyId() {
        return legacyId;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public PaperReference getPaperReference() {
        return paperReference;
    }

    public String getResultUnit() {
        return resultUnit;
    }

    public String getResultType() {
        return resultType;
    }


    public String getHazardType() {
        return hazardType;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
