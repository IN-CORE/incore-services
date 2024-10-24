/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.parser;

import edu.illinois.ncsa.incore.semantic.units.dimension.BaseDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.DerivedDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimensionless;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.RDFModel;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RDFParser {
    private RDFParser() {
    }

    /**
     *
     */
    public static List<Unit> parseUnits(String rdf) {
        List<Unit> units = new ArrayList<>();

        InputStream stream = new ByteArrayInputStream(rdf.getBytes());

        Model model = ModelFactory.createDefaultModel();
        model.read(stream, "", "TTL");

        for (ResIterator iterator = model.listResourcesWithProperty(RDF.type); iterator.hasNext(); ) {
            Resource resource = iterator.next();

            String name = resource.getProperty(RDFS.label).getObject().asLiteral().getString();
            String symbol = resource.getProperty(RDFModel.symbol).getObject().asLiteral().getString();
            String unicodeSymbol = resource.getProperty(RDFModel.symbolUnicode).getObject().asLiteral().getString();
            String typeURI = resource.getProperty(RDF.type).getObject().asResource().getLocalName();
        }

        return units;
    }

    public static List<Dimension> parseDimensions(String rdf) {
        List<Dimension> dimensions = new ArrayList<>();

        Model model = ModelFactory.createDefaultModel();

        InputStream stream = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));
        model.read(stream, "", "TTL");

        for (ResIterator iterator = model.listResourcesWithProperty(RDF.type); iterator.hasNext(); ) {
            Resource resource = iterator.next();

            Optional<Dimension> parseResult = parseDimension(resource);

            if (parseResult.isPresent()) {
                dimensions.add(parseResult.get());
            }
        }

        return dimensions;
    }

    /**
     * Attempts to parse a dimension from a Jena Resource object.
     * Will first check if the resource name is present in the existing models,
     * If so it will return that instance, if not it will create a new dimension object.
     */
    private static Optional<Dimension> parseDimension(Resource resource) {
        // Parse values from RDF Jena Model
        String resourceName = resource.getLocalName();
        String name = resource.getProperty(RDFS.label).getObject().asLiteral().getString();
        String symbol = resource.getProperty(RDFModel.symbol).getObject().asLiteral().getString();
        String unicodeSymbol = resource.getProperty(RDFModel.symbolUnicode).getObject().asLiteral().getString();
        String typeURI = resource.getProperty(RDF.type).getObject().asResource().getLocalName();

        // Check if dimension already exists
        Optional<Dimension> result = Dimensions.getByResourceName(resourceName);

        if (result.isPresent()) {
            return result;
        }

        // If dimension doesn't already exist then create it
        if (typeURI.equals("Dimensionless")) {
            Dimensionless dimension = new Dimensionless(name);
            return Optional.of(dimension);
        } else if (typeURI.equals("DerivedDimension")) {
            Map<BaseDimension, Integer> bases = DimensionParser.parseSymbol(symbol);
            DerivedDimension dimension = new DerivedDimension(name, bases);
            return Optional.of(dimension);
        } else {
            return Optional.empty();
        }
    }
}
