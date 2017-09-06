package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;

@XmlRootElement
public class MappingResponse {
    @XmlElement(name = "sets")
    public List<FragilitySet> fragilitySets;

    @XmlElement(name = "mapping")
    public HashMap<Integer, String> fragilityToInventoryMapping;
}
