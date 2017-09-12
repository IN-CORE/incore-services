package edu.illinois.ncsa.incore.services.maestro.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

@XmlRootElement
public class ServiceRequest {

    @XmlElement(name="params")
    public HashMap<String, Object> parameters = new HashMap<>();

    public ServiceRequest() {}

    public ServiceRequest(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }
}
