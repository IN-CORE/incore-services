package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@XmlRootElement
public class MappingResponse {
    @XmlElement(name = "sets")
    public Map<String, FragilitySet> fragilitySets = new HashMap<>();

    @XmlElement(name = "mapping")
    public Map<String, String> fragilityToInventoryMapping = new HashMap<>();

    public MappingResponse() {

    }

    public MappingResponse(Map<String, FragilitySet> fragilitySets, Map<String, String> fragilityMap) {
        this.fragilitySets = fragilitySets;
        this.fragilityToInventoryMapping = fragilityMap;
    }

    public Map<String, FragilitySet> getFragilitySets() {
        return fragilitySets;
    }

    public Map<String, String> getFragilityToInventoryMapping() {
        return fragilityToInventoryMapping;
    }
}
