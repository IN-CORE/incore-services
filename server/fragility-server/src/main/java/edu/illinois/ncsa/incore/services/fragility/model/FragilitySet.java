package edu.illinois.ncsa.incore.services.fragility.model;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FragilitySet {
    @Id
    @Property("_id")
    private String id;

    @XmlTransient
    private String legacyId;

    private String description;

    private List<String> authors = new ArrayList<>();
    private PaperReference paperReference;

    private String resultUnit;
    private String resultType;
    private String demandType;
    private String demandUnits;

    private String hazardType;
    private String inventoryType;

    private List<FragilityCurve> fragilityCurves;

    public FragilitySet() { }

    public FragilitySet(String legacyId, String description, List<String> authors,
                        PaperReference paperReference, String resultUnit, String resultType, String demandType, String demandUnits,
                        String hazardType, String inventoryType,
                        List<FragilityCurve> fragilityCurves) {
        this.legacyId = legacyId;
        this.description = description;
        this.authors = authors;
        this.paperReference = paperReference;
        this.resultUnit = resultUnit;
        this.resultType = resultType;
        this.demandType = demandType;
        this.demandUnits = demandUnits;
        this.hazardType = hazardType;
        this.inventoryType = inventoryType;
        this.fragilityCurves = fragilityCurves;
    }

    // region Getters
    public String getId() {
        return id;
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

    public String getDemandType() {
        return demandType;
    }

    public String getDemandUnits() {
        return demandUnits;
    }

    public String getHazardType() {
        return hazardType;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public List<FragilityCurve> getFragilityCurves() {
        return fragilityCurves;
    }
    // endregion
}
