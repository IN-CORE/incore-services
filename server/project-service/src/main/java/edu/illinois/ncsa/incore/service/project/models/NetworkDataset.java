package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkDataset {
    private NetworkData link;
    private NetworkData node;
    private NetworkData graph;
}
