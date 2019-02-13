/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.validation.rules;

public enum ComparatorType {
    Equal("EQ", "Equal", "=", "="),
    GreaterThan("GT", "Greater Than", ">", ">"),
    GreaterThanOrEqual("GE", "Greater Than Or Equal", ">=", "\u2265"),
    LessThan("LT", "Less Than", "<", "<"),
    LessThanOrEqual("LE", "Less Than or Equal", "<=", "\u2264"),
    NotEqual("NE", "Not Equal", "!=", "\u2260");

    private String shortName;
    private String name;
    private String symbol;
    private String unicodeSymbol;

    private ComparatorType(String shortName, String name, String symbol, String unicodeSymbol) {
        this.shortName = shortName;
        this.name = name;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }
}
