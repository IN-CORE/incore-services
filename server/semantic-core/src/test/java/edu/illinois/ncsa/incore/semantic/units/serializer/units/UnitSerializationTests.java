/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.serializer.units;

import edu.illinois.ncsa.incore.semantic.units.model.PrefixedUnit;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.centi;
import static edu.illinois.ncsa.incore.semantic.units.instances.Prefixes.kilo;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.*;
import static edu.illinois.ncsa.incore.semantic.units.instances.SIUnits.metre;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnitSerializationTests {
    ClassLoader loader = this.getClass().getClassLoader();

    /**
     * Compares two Turtle documents as RDF graphs, since Jena does not guarantee the order of @prefix declarations
     */
    private static void assertSameGraph(String expected, String actual) {
        Model expectedModel = ModelFactory.createDefaultModel().read(new StringReader(expected), null, "TURTLE");
        Model actualModel = ModelFactory.createDefaultModel().read(new StringReader(actual), null, "TURTLE");
        assertTrue(expectedModel.isIsomorphicWith(actualModel), () -> "expected: <" + expected + "> but was: <" + actual + ">");
    }

    @Test
    @DisplayName("Serialize Prefixed Unit - centimetre")
    public void testSerializePrefixedUnit() throws IOException {
        File file = new File(loader.getResource("units/centimetre.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        Unit unit = new PrefixedUnit(centi, metre);
        String expected = unit.serialize();

        assertSameGraph(expected, actual);
    }

    @Test
    @DisplayName("Serialize Prefixed Coherent Derived Unit - kilojoule")
    public void testSerializePrefixedCoherentDerivedUnit() throws IOException {
        File file = new File(loader.getResource("units/kilojoule.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        Unit unit = new PrefixedUnit(kilo, joule);
        String expected = unit.serialize();

        assertSameGraph(expected, actual);
    }

    @Test
    @DisplayName("Serialize Prefixable Unit - metre")
    public void testSerializePrefixableUnit() throws IOException {
        File file = new File(loader.getResource("units/metre.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = metre.serialize();

        assertSameGraph(expected, actual);
    }

    @Test
    @DisplayName("Serialize Named Derived Unit - volt")
    public void testSerializeNamedDerived() throws IOException {
        File file = new File(loader.getResource("units/volt.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = volt.serialize();

        assertSameGraph(expected, actual);
    }

    @Test
    @DisplayName("Serialize Product Derived Unit - newton metre second")
    public void testSerializeProductDerived() throws IOException {
        File file = new File(loader.getResource("units/newton_metre_second.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = newtonMetreSecond.serialize();

        assertSameGraph(expected, actual);
    }

    @Test
    @DisplayName("Serialize Dimensionless Unit - radian")
    public void testSerializeDimensionless() throws IOException {
        File file = new File(loader.getResource("units/radian.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = radian.serialize();

        assertSameGraph(expected, actual);
    }
}
