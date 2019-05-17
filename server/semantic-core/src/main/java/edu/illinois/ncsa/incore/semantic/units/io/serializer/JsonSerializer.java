/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.illinois.ncsa.incore.semantic.units.Prefix;
import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.io.Writer;

public class JsonSerializer implements ISerializer {

    public JsonNode serializeToNode(Unit unit) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("name", unit.getName());
        root.put("_type", unit.getClass().getSimpleName());
        root.put("unicodeName", unit.getUnicodeName());
        root.put("plural", unit.getPlural());
        root.put("unicodePlural", unit.getUnicodePlural());
        root.put("symbol", unit.getSymbol());
        root.put("unicodeSymbol", unit.getUnicodeSymbol());

        root.put("dimension", unit.getDimension().getResourceName());
        root.put("unitSystem", unit.getUnitSystem().getName());

        return root;
    }

    public JsonNode serializeToNode(Dimension dimension) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("name", dimension.getName());
        root.put("_type", dimension.getClass().getSimpleName());
        root.put("symbol", dimension.getSymbol());
        root.put("unicodeSymbol", dimension.getUnicodeSymbol());

        return root;
    }

    public JsonNode serializeToNode(Prefix prefix) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("name", prefix.getName());
        root.put("symbol", prefix.getSymbol());
        root.put("unicodeSymbol", prefix.getUnicodeSymbol());

        return root;
    }

    @Override
    public String serialize(Unit unit) {
        JsonNode root = serializeToNode(unit);
        return root.toString();
    }

    @Override
    public String serialize(Dimension dimension) {
        JsonNode root = serializeToNode(dimension);
        return root.toString();
    }

    @Override
    public String serialize(Prefix prefix) {
        JsonNode root = serializeToNode(prefix);
        return root.toString();
    }

    @Override
    public void serialize(Unit unit, Writer writer) {

    }

    @Override
    public void serialize(Dimension dimension, Writer writer) {

    }

    @Override
    public void serialize(Prefix prefix, Writer writer) {

    }
}
