/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.serializer;

import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.io.Writer;

public interface ISerializer {
    String serialize(Unit unit);

    String serialize(Dimension dimension);

    String serialize(Prefix prefix);

    void serialize(Unit unit, Writer writer);

    void serialize(Dimension dimension, Writer writer);

    void serialize(Prefix prefix, Writer writer);
}
