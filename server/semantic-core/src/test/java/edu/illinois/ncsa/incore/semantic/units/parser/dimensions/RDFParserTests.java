/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.parser.dimensions;

import edu.illinois.ncsa.incore.semantic.units.dimension.BaseDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.DerivedDimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimensionless;
import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import edu.illinois.ncsa.incore.semantic.units.io.parser.RDFParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RDFParserTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    public void testSerialize_BaseDimension() throws IOException {
        // arrange
        File file = new File(loader.getResource("dimensions/length.ttl").getFile());
        String rdf = FileUtils.readFileToString(file, "UTF-8");
        BaseDimension expected = Dimensions.length;

        // act
        Dimension actual = RDFParser.parseDimensions(rdf).get(0);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    public void testSerialize_Dimensionless() throws IOException {
        // arrange
        File file = new File(loader.getResource("dimensions/angle.ttl").getFile());
        String rdf = FileUtils.readFileToString(file, "UTF-8");
        Dimensionless expected = Dimensions.angle;

        // act
        Dimension actual = RDFParser.parseDimensions(rdf).get(0);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    public void testSerialize_DerivedDimension() throws IOException {
        // arrange
        File file = new File(loader.getResource("dimensions/current_density.ttl").getFile());
        String rdf = FileUtils.readFileToString(file, "UTF-8");
        DerivedDimension expected = Dimensions.electricCurrentDensity;

        // act
        Dimension actual = RDFParser.parseDimensions(rdf).get(0);

        // assert
        assertEquals(expected, actual);
    }
}
