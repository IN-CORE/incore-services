package Semantic.Units.Conversion.Engine;

import Semantic.Units.Conversion.Operations.MultiplyOperation;
import Semantic.Units.Conversion.Operations.Operation;
import Semantic.Units.Model.Unit;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

import java.util.ArrayList;
import java.util.List;

// TODO use multiple graphs implemented as a Map<Dimension, Graph>
final class GraphSolver {

    private static DirectedGraph<Unit, Operation> graph = new DefaultDirectedGraph<>(Operation.class);
    private static DijkstraShortestPath<Unit, Operation> shortestPath = new DijkstraShortestPath<>(graph);

    private GraphSolver() {
    }

    static {

    }

    static void initialize() {
        // do nothing java will invoke static constructor
    }

    static void addEdge(Unit fromUnit, Unit toUnit, Operation operation) {
        if (!graph.containsVertex(fromUnit)) {
            graph.addVertex(fromUnit);
        }

        if (!graph.containsVertex(toUnit)) {
            graph.addVertex(toUnit);
        }

        graph.addEdge(fromUnit, toUnit, operation);
    }

    static Operation getEdge(Unit from, Unit to) {
        return graph.getEdge(from, to);
    }

    static List<Operation> getEdges(Unit fromUnit, Unit toUnit) {
        // get the shortest path between the two units
        if (!graph.containsVertex(fromUnit)) {
            throw new IllegalArgumentException("Unit " + fromUnit.getName() + " not registered in unit converter");
        }

        if (!graph.containsVertex(toUnit)) {
            throw new IllegalArgumentException("Unit " + toUnit.getName() + " not registered in unit converter");
        }

        GraphPath<Unit, Operation> path = shortestPath.getPath(fromUnit, toUnit);

        List<Operation> operations = path.getEdgeList();

        if (operations.size() > 0) {
            return operations;
        } else {
            throw new IllegalArgumentException(
                    "No direct or indirect path found between " + fromUnit.getName() + " and " + toUnit.getName());
        }
    }

    static List<MultiplyOperation> getLinearEdges(Unit fromUnit, Unit toUnit) {
        if (containsLinearPath(fromUnit, toUnit)) {
            List<Operation> operations = getEdges(fromUnit, toUnit);
            List<MultiplyOperation> returnOperations = new ArrayList<>(operations.size());
            for (Operation operation : operations) {
                returnOperations.add((MultiplyOperation) operation);
            }

            return returnOperations;
        } else {
            throw new IllegalArgumentException("non-linear operation detected in shortest path");
        }
    }

    static boolean containsEdge(Unit fromUnit, Unit toUnit) {
        return graph.containsEdge(fromUnit, toUnit);
    }

    static boolean containsPath(Unit fromUnit, Unit toUnit) {
        if (!graph.containsVertex(fromUnit) || !graph.containsVertex(toUnit)) {
            return false;
        }

        return getEdges(fromUnit, toUnit).size() > 0;
    }

    static boolean containsLinearPath(Unit fromUnit, Unit toUnit) {
        List<Operation> operations = getEdges(fromUnit, toUnit);

        return (operations.size() > 0 && operations.stream().allMatch(op -> op instanceof MultiplyOperation));
    }

    static double convert(Unit fromUnit, Unit toUnit, Number value) {
        List<Operation> operations = getEdges(fromUnit, toUnit);

        double returnValue = value.doubleValue();
        for (Operation operation : operations) {
            returnValue = operation.invoke(returnValue);
        }

        return returnValue;
    }

    static double getConversionFactor(Unit fromUnit, Unit toUnit) {
        List<MultiplyOperation> operations = getLinearEdges(fromUnit, toUnit);

        double factor = 1;

        for (MultiplyOperation operation : operations) {
            factor *= operation.getFactor();
        }

        return factor;
    }

}
