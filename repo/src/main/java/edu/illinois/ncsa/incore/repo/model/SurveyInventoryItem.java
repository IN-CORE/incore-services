package edu.illinois.ncsa.incore.repo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@Entity("surveyinventory")
@XmlRootElement
public class SurveyInventoryItem {
    @Id
    ObjectId id;

    public List<String> schemas;
    public Map<String,String> attributes;

}
