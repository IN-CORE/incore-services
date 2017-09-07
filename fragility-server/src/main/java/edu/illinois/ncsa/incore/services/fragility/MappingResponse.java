package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class MappingResponse {
    @XmlElement(name = "sets")
    public List<FragilitySet> fragilitySets;

    @XmlElement(name = "mapping")
    public Map<String, String> fragilityToInventoryMapping;

    public MappingResponse(Map<String, String> fragilityMap) {
        this.fragilityToInventoryMapping = fragilityMap;
    }
}
