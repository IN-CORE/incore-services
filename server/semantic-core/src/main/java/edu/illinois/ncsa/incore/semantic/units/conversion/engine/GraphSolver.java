/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.conversion.engine;

import edu.illinois.ncsa.incore.semantic.units.conversion.operations.MultiplyOperation;
import edu.illinois.ncsa.incore.semantic.units.conversion.operations.Operation;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO use multiple graphs implemented as a Map<edu.illinois.ncsa.incore.semantic.units.Dimension, Graph>
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

        if (path != null) {
            List<Operation> operations = path.getEdgeList();

            if (operations.size() > 0) {
                return operations;
            }
        }

        // Return an empty list
        return new ArrayList<>();
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
        if (graph.containsEdge(fromUnit, toUnit)) {
            return true;
        }

        if (!graph.containsVertex(fromUnit) || !graph.containsVertex(toUnit)) {
            return false;
        }

        return getEdges(fromUnit, toUnit).size() > 0;
    }

    static boolean containsLinearPath(Unit fromUnit, Unit toUnit) {
        List<Operation> operations = getEdges(fromUnit, toUnit);

        return (operations.size() > 0 && operations.stream().allMatch(op -> op instanceof MultiplyOperation));
    }

    static Optional<Double> tryConvert(Unit fromUnit, Unit toUnit, Number value) {
        if (containsEdge(fromUnit, toUnit)) {
            double convertedValue = getEdge(fromUnit, toUnit).invoke(value.doubleValue());
            return Optional.of(convertedValue);
        } else {
            List<Operation> operations = getEdges(fromUnit, toUnit);

            if (operations.size() == 0) {
                return Optional.empty();
            } else {
                double convertedValue = value.doubleValue();
                for (Operation operation : operations) {
                    convertedValue = operation.invoke(convertedValue);
                }

                return Optional.of(convertedValue);
            }
        }
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
