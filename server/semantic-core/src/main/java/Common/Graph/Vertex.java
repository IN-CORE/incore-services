package Common.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Vertex<V, E> {
    private V vertexData;
    private List<Edge<E>> edges = new ArrayList<>();

    public Vertex(V vertexData) {
        this.vertexData = vertexData;
    }

    public Vertex(V vertex, List<Edge<E>> edgesToAdd) {
        this.vertexData = vertex;
        edges.addAll(edgesToAdd);
    }

    public boolean hasEdge(Vertex toVertex) {
        for (Edge<E> edge : edges) {
            if (edge.getDestinationNode().equals(toVertex)) {
                return true;
            }
        }

        return false;
    }

    public V getVertexData() {
        return this.vertexData;
    }

    public List<E> getEdges() {
        return this.edges.stream()
                         .map(Edge::getEdgeData)
                         .collect(Collectors.toList());
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
}
