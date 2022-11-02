package edu.illinois.ncsa.incore.service.dfr3.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public abstract class DFR3Set {
    @Id
    @Property("_id")
    protected ObjectId id;

    @XmlTransient
    protected String legacyId;

    protected String description;

    protected List<String> authors = new ArrayList<>();
    protected PaperReference paperReference;

    // TODO this field now is moved to returnType field on each curve
    protected String resultUnit;
    protected String resultType;


    protected String hazardType;
    protected String inventoryType;

    protected String creator;
    protected String owner;

    protected List<CurveParameter> curveParameters;

    /**
     * spaces the object belongs to. Calculated at runtime.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    public String getId() {
        return (id == null) ? null : id.toString();
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    public List<CurveParameter> getCurveParameters() {
        return curveParameters;
    }

    public void setCurveParameters(List<CurveParameter> curveParameters) {
        this.curveParameters = curveParameters;
    }

}
