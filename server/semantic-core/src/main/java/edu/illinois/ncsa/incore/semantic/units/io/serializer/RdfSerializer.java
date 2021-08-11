/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.serializer;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.io.RDFFormat;
import edu.illinois.ncsa.incore.semantic.units.io.RDFModel;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixableUnit;
import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.OperatorDerivedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.derived.PowerDerivedUnit;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class RdfSerializer implements ISerializer {
    private final RDFFormat format;

    public RdfSerializer() {
        this.format = RDFFormat.TURTLE;
    }

    public RdfSerializer(RDFFormat format) {
        this.format = format;
    }

    /**
     * Returns a serialized dimension object in RDF Turtle format
     */
    @Override
    public String serialize(Dimension dimension) {
        StringWriter writer = new StringWriter();
        serialize(dimension, writer);
        String output = writer.toString();
        return output;
    }

    @Override
    public String serialize(Prefix prefix) {
        StringWriter writer = new StringWriter();
        serialize(prefix, writer);
        String output = writer.toString();
        return output;
    }

    /**
     * Serializes a dimension object and writes it to the input writer object, in the specified RDF Format.
     *
     * @param writer The Writer to serialize to
     */
    public void serialize(Dimension dimension, Writer writer) {
        Resource resource = getResource(dimension);

        Model model = resource.getModel();
        model.write(writer, format.getValue());
    }

    @Override
    public void serialize(Prefix prefix, Writer writer) {

    }

    /**
     * Returns a serialized unit object in RDF Turtle format
     */
    @Override
    public String serialize(Unit unit) {
        StringWriter writer = new StringWriter();
        serialize(unit, writer);
        String output = writer.toString();
        return output;
    }

    /**
     * Serializes a unit object and writes it to the input writer object, in the specified RDF Format.
     *
     * @param writer The Writer to serialize to
     */
    public void serialize(Unit unit, Writer writer) {
        Resource resource = getResource(unit);

        Model model = resource.getModel();
        model.write(writer, format.getValue());
    }

    /**
     * Returns a list of serialize unit objects in RDF Turtle format
     */
    public void serialize(List<Unit> units, Writer writer) {
        if (units.size() > 0) {
            Resource resource = getResource(units.get(0));
            Model model = resource.getModel();

            for (int i = 1; i < units.size(); i++) {
                Resource unitResource = getResource(units.get(i));
                model.add(unitResource.getModel());
            }

            model.write(writer, format.getValue());
        }
    }

    /**
     * Converts a dimension object to a RDF Resource object
     */
    public static Resource getResource(Dimension dimension) {
        String uri = RDFModel.dimensionURI + dimension.getResourceName();

        // Namespaces
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("dim", RDFModel.dimensionURI);
        model.setNsPrefix("def", RDFModel.schemaURI);

        // Resources
        Resource dimensionTypeResource = new ResourceImpl(RDFModel.schemaURI, dimension.getClass().getSimpleName());

        // Model
        Resource dimensionResource = model.createResource(uri);
        dimensionResource.addProperty(RDF.type, dimensionTypeResource);
        dimensionResource.addProperty(RDFS.label, model.createLiteral(dimension.getName(), "en"));
        dimensionResource.addProperty(RDFModel.symbol, dimension.getSymbol());
        dimensionResource.addProperty(RDFModel.symbolUnicode, dimension.getUnicodeSymbol());

        return dimensionResource;
    }

    /**
     * Converts a Unit object to an RDF Resource object
     */
    public static Resource getResource(Unit unit) {
        String uri = RDFModel.unitURI + unit.getResourceName();

        // Namespaces
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("unit", RDFModel.unitURI);
        model.setNsPrefix("dim", RDFModel.dimensionURI);
        model.setNsPrefix("def", RDFModel.schemaURI);

        // Resources
        Resource unitTypeResource = new ResourceImpl(RDFModel.schemaURI, unit.getClass().getSimpleName());
        Resource dimensionResource = getResource(unit.getDimension());

        // Model
        Resource unitResource = model.createResource(uri);
        unitResource.addProperty(RDF.type, unitTypeResource);
        unitResource.addProperty(RDFS.label, model.createLiteral(unit.getName(), "en"));
        unitResource.addProperty(RDFModel.symbol, unit.getSymbol());
        unitResource.addProperty(RDFModel.symbolUnicode, unit.getUnicodeSymbol());
        unitResource.addProperty(RDFModel.plural, model.createLiteral(unit.getPlural(), "en"));
        unitResource.addProperty(RDFModel.pluralUnicode, model.createLiteral(unit.getUnicodePlural(), "en"));
        unitResource.addProperty(RDFModel.unitSystem, unit.getUnitSystem().name());
        unitResource.addProperty(RDFModel.dimension, dimensionResource);

        if (unit instanceof PrefixedUnit) {
            model.setNsPrefix("prefix", RDFModel.prefixURI);

            PrefixedUnit castUnit = (PrefixedUnit) unit;
            Resource prefix = getResource(castUnit.getPrefix());
            Resource base = getResource(castUnit.getBaseUnit());

            unitResource.addProperty(RDFModel.prefix, prefix);
            unitResource.addProperty(RDFModel.baseUnit, base);
        } else if (unit instanceof PrefixableUnit) {
            model.setNsPrefix("prefix", RDFModel.prefixURI);

            PrefixableUnit castUnit = (PrefixableUnit) unit;

            List<Prefix> prefixes = castUnit.getApplicablePrefixes();

            RDFNode[] prefixesArray = new RDFNode[prefixes.size()];

            for (int i = 0; i < prefixesArray.length; i++) {
                prefixesArray[i] = getResource(prefixes.get(i));
            }

            RDFList prefixesResource = model.createList(prefixesArray);

            unitResource.addProperty(RDFModel.applicablePrefixes, prefixesResource);
        } else if (unit instanceof OperatorDerivedUnit) {
            OperatorDerivedUnit castUnit = (OperatorDerivedUnit) unit;
            Resource leftOperand = getResource(castUnit.leftOperand);
            Resource rightOperand = getResource(castUnit.rightOperand);

            unitResource.addProperty(RDFModel.leftOperand, leftOperand);
            unitResource.addProperty(RDFModel.rightOperand, rightOperand);
        } else if (unit instanceof PowerDerivedUnit) {
            PowerDerivedUnit castUnit = (PowerDerivedUnit) unit;
            Resource operand = getResource(castUnit.getOperand());

            unitResource.addProperty(RDFModel.operand, operand);
            unitResource.addProperty(RDFModel.power, model.createTypedLiteral(castUnit.getPower()));
        }

        return unitResource;
    }

    /**
     * Converts a Prefix object to an RDF Resource object
     */
    public static Resource getResource(Prefix prefix) {
        String uri = RDFModel.prefixURI + prefix.getResourceName();

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("unit", RDFModel.unitURI);
        model.setNsPrefix("dim", RDFModel.dimensionURI);

        model.setNsPrefix("def", RDFModel.schemaURI);

        Resource prefixResource = model.createResource(uri);
        prefixResource.addProperty(RDF.type, RDFModel.schemaURI + "Prefix");
        prefixResource.addProperty(RDFS.label, model.createLiteral(prefix.getName(), "en"));
        prefixResource.addProperty(RDFModel.symbol, prefix.getSymbol());
        prefixResource.addProperty(RDFModel.symbolUnicode, prefix.getUnicodeSymbol());

        return prefixResource;
    }
}
