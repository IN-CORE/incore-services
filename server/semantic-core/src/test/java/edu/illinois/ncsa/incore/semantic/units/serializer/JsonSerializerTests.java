/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.serializer;

import edu.illinois.ncsa.incore.semantic.units.io.serializer.JsonSerializer;
import org.junit.jupiter.api.Test;

import static edu.illinois.ncsa.incore.semantic.units.instances.SIDerivedUnits.squareMetre;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSerializerTests {
    ClassLoader loader = this.getClass().getClassLoader();

    @Test
    public void test() {
        JsonSerializer serializer = new JsonSerializer();
        String value = serializer.serialize(squareMetre);

        assertTrue(true);
    }
}
