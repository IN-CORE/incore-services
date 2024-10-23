package edu.illinois.ncsa.incore.service.project.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class NetworkDataset {
    private NetworkData link;
    private NetworkData node;
    private NetworkData graph;
}
