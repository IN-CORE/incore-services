package Common.Graph;

import java.util.Set;

public class Graph<V, E> {
    private Set<Vertex> vertices;

    public void addVertex(V vertexToAdd) {
        Vertex<V, E> vertex = new Vertex<V, E>(vertexToAdd);
        vertices.add(vertex);
    }

    public void addEdge(V fromNode, V toNode, E edgeToAdd) {
        if (!doesVertexExist(fromNode)) {
            addVertex(fromNode);
        }

        if (!doesVertexExist(toNode)) {
            addVertex(toNode);
        }

        Vertex fromVertex = getVertex(fromNode);
        Vertex toVertex = getVertex(toNode);

        Edge<E> outgoingEdge = new Edge<>(toVertex, edgeToAdd);
        fromVertex.addEdge(outgoingEdge);
        Edge<E> incomingEdge = new Edge<>(fromVertex, edgeToAdd);
        toVertex.addEdge(incomingEdge);
    }

    public boolean doesVertexExist(V match) {
        for (Vertex vertex : vertices) {
            if (vertex.getVertexData().equals(match)) {
                return true;
            }
        }

        return false;
    }

    private Vertex getVertex(V match) {
        for (Vertex vertex : vertices) {
            if (vertex.getVertexData().equals(match)) {
                return vertex;
            }
        }

        throw new IllegalArgumentException("Could not find vertex in graph" );
    }

    public boolean doesEdgeExist(V fromNode, V toNode) {
        if (doesVertexExist(fromNode) && doesVertexExist(toNode)) {
            Vertex fromVertex = getVertex(fromNode);
            Vertex toVertex = getVertex(toNode);
            return fromVertex.hasEdge(toVertex);
        } else {
            return false;
        }
    }
}

