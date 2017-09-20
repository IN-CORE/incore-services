package Common.Graph;

// Node, Edge, Node
public class Edge<E> {
    private E edgeData;
    private Vertex toNode;

    public Edge(Vertex toNode, E edgeData) {
        this.toNode = toNode;
        this.edgeData = edgeData;
    }

    public E getEdgeData() {
        return this.edgeData;
    }

    public Vertex getDestinationNode() {
        return this.toNode;
    }
}
