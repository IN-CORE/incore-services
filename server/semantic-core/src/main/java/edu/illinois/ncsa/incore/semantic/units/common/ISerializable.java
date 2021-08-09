/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.common;

import edu.illinois.ncsa.incore.semantic.units.io.serializer.ISerializer;

import java.io.Writer;

public interface ISerializable {
    String serialize();

    String serialize(ISerializer serializer);

    void serialize(ISerializer serialize, Writer writer);
}
