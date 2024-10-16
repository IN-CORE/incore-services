/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.serializer.dimensions;

import edu.illinois.ncsa.incore.semantic.units.instances.Dimensions;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DimensionSerializationTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    @DisplayName("serialize length (base dimension)")
    public void testSerialize_BaseDimension() throws IOException {
        File file = new File(loader.getResource("dimensions/length.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = Dimensions.length.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("serialize angle (dimensionless)")
    public void testSerialize_Dimensionless() throws IOException {
        File file = new File(loader.getResource("dimensions/angle.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = Dimensions.angle.serialize();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("serialize current_density (derived dimension)")
    public void testSerialize_DerivedDimension() throws IOException {
        File file = new File(loader.getResource("dimensions/current_density.ttl").getFile());
        String actual = FileUtils.readFileToString(file, "UTF-8");

        String expected = Dimensions.electricCurrentDensity.serialize();

        assertEquals(expected, actual);
    }
}
