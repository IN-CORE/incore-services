package edu.illinois.ncsa.incore.services.fragility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

public class MappingResponse {
    @JsonProperty("sets")
    public Map<String, FragilitySet> fragilitySets = new HashMap<>();

    @JsonProperty("mapping")
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
