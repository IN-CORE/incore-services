package edu.illinois.ncsa.Fragility.Model;

import edu.illinois.ncsa.Fragility.JaxPModel.FragilityDataset;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FragilitySet {
    @Id
    @XmlTransient
    public ObjectId mongoId;

    public String id; // base

    public String author; // base
    public String description; // base

    public String resultUnit;
    public String resultType;
    public String demandType;
    public String demandUnits;

    public FragilitySet() { }

    public FragilitySet(String id, String author, String description) {
        this.id = id;
        this.author = author;
        this.description = description;
    }

    public List<FragilityCurve> fragilityCurves;

    public static List<FragilitySet> parse(FragilityDataset dataset) {
        List<FragilitySet> sets = new ArrayList<>();

        List<FragilityDataset.FragilityDatasetSets.FragilitySet> fragilitySets = dataset.getFragilityDatasetSets().getFragilitySet();

        for (FragilityDataset.FragilityDatasetSets.FragilitySet fragilitySet : fragilitySets) {
            FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetProperties properties = fragilitySet.getFragilitySetProperties();
            FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetLabels labels = fragilitySet.getFragilitySetLabels();
            FragilityDataset.FragilityDatasetSets.FragilitySet.FragilitySetFragilities curves = fragilitySet.getFragilitySetFragilities();

            String id = properties.getID();
            String author = properties.getAuthor();
            String description = properties.getDescription();

            FragilitySet createdSet = new FragilitySet(id, author, description);
            createdSet.demandType = properties.getDemandType();
            createdSet.demandUnits = properties.getDemandUnits();

            if (properties.getResultType() != null) {
                createdSet.resultType = properties.getResultType();
            } else {
                createdSet.resultType = "Limit State";
            }

            createdSet.fragilityCurves = FragilityCurve.parseCurves(labels, curves);

            sets.add(createdSet);
        }

        return sets;
    }
}
