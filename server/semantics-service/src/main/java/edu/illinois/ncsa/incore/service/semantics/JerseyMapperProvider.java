/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.semantics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.illinois.ncsa.incore.semantic.units.io.serializer.json.JsonUnitSerializer;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JerseyMapperProvider implements ContextResolver<ObjectMapper> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ObjectMapper getContext(Class<?> type) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Unit.class, new JsonUnitSerializer());
        mapper.registerModule(module);

        return mapper;
    }
}
