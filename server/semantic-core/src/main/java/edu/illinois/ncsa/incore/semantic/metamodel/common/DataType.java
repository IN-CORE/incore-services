/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.common;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public enum DataType {
    STRING("string", String.class),
    INT("int", int.class),
    DOUBLE("double", double.class),
    DECIMAL("decimal", BigDecimal.class),
    DATETIME("datetime", LocalDateTime.class),
    TIMESTAMP("timestamp", Timestamp.class);

    private final String name;
    private final Class clazz;

    DataType(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}
