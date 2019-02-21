/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.units.io.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.io.IOException;

public class JsonUnitSerializer extends JsonSerializer<Unit> {

    @Override
    public void serialize(Unit unit, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();

        gen.writeStringField("name", unit.getName());
        gen.writeStringField("_type", unit.getClass().getSimpleName());
        gen.writeStringField("unicodeName", unit.getUnicodeName());
        gen.writeStringField("plural", unit.getPlural());
        gen.writeStringField("unicodePlural", unit.getUnicodePlural());
        gen.writeStringField("symbol", unit.getSymbol());
        gen.writeStringField("unicodeSymbol", unit.getUnicodeSymbol());

        gen.writeStringField("dimension", unit.getDimension().getResourceName());
        gen.writeStringField("unitSystem", unit.getUnitSystem().getName());

        gen.writeEndObject();
    }
}
