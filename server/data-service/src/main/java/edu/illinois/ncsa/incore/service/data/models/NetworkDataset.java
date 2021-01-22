package edu.illinois.ncsa.incore.service.data.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class NetworkDataset {
    private NetworkData link;
    private NetworkData node;
    private NetworkData graph;

    public NetworkData getLink() {
        return link;
    }

    public void setLink(NetworkData link) {
        this.link = link;
    }

    public NetworkData getNode() {
        return node;
    }

    public void setNode(NetworkData node) {
        this.node = node;
    }

    public NetworkData getGraph() {
        return graph;
    }

    public void setGraph(NetworkData graph) {
        this.graph = graph;
    }
}
