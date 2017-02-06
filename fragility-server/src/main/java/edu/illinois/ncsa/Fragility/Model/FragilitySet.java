package edu.illinois.ncsa.Fragility.Model;

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
    public String id;

    @XmlTransient
    public String legacyId;

    public String description;

    public List<String> authors = new ArrayList<>();
    public PaperReference paperReference;

    public String resultUnit;
    public String resultType;
    public String demandType;
    public String demandUnits;

    public String hazardType;
    public String inventoryType;

    public List<FragilityCurve> fragilityCurves;


    public FragilitySet() { }
}
