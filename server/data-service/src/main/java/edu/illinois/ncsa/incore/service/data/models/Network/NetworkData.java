package edu.illinois.ncsa.incore.service.data.models.Network;

public class NetworkData {
    private String nodeType;
    private String linkType;
    private String graphType;

    private String nodeFileName;
    private String linkFileName;
    private String graphFileName;


    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeFileName() {
        return nodeFileName;
    }

    public void setNodeFileName(String nodeFileName) {
        this.nodeFileName = nodeFileName;
    }


    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getLinkFileName() {
        return linkFileName;
    }

    public void setLinkFileName(String linkFileName) {
        this.linkFileName = linkFileName;
    }


    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public String getGraphFileName() {
        return graphFileName;
    }

    public void setGraphFileName(String graphFileName) {
        this.graphFileName = graphFileName;
    }

}
