package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkData {
    private String dataType;
    private String fileName;

}
