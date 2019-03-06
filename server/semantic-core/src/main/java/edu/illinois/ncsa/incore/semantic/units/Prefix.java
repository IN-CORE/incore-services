/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units;

import edu.illinois.ncsa.incore.semantic.units.common.ISerializable;
import edu.illinois.ncsa.incore.semantic.units.common.ISymbol;
import edu.illinois.ncsa.incore.semantic.units.io.RDFFormat;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.ISerializer;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.RdfSerializer;

import java.io.Writer;
import java.util.Objects;

public class Prefix implements ISymbol, ISerializable {
    private String resourceName;
    private String name;
    private String symbol;
    private String unicodeSymbol;
    private int base;
    private int scale;

    public Prefix(String name, String symbol, String unicodeSymbol, int base, int scale) {
        this.resourceName = name;
        this.name = name;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
        this.base = base;
        this.scale = scale;
    }

    public Prefix(String resourceName, String name, String symbol, String unicodeSymbol, int base, int scale) {
        this.resourceName = resourceName;
        this.name = name;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
        this.base = base;
        this.scale = scale;
    }

    //region Serialization Methods
    @Override
    public String serialize() {
        ISerializer rdfSerializer = new RdfSerializer(RDFFormat.TURTLE);
        String output = rdfSerializer.serialize(this);
        return output;
    }

    @Override
    public String serialize(ISerializer serializer) {
        return serializer.serialize(this);
    }

    @Override
    public void serialize(ISerializer serializer, Writer writer) {
        serializer.serialize(this);
    }
    //endregion

    //region Getters
    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }

    public int getScale() {
        return scale;
    }

    public int getBase() {
        return base;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.getSymbol() + ")";
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }
    //endregion

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Prefix)) {
            return false;
        }

        Prefix prefix = (Prefix) obj;

        return base == prefix.base &&
                scale == prefix.scale &&
                Objects.equals(resourceName, prefix.resourceName) &&
                Objects.equals(name, prefix.name) &&
                Objects.equals(symbol, prefix.symbol) &&
                Objects.equals(unicodeSymbol, prefix.unicodeSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, name, symbol, unicodeSymbol, base, scale);
    }
}
