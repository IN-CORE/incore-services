/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.FeatureCollection;

import java.util.HashMap;

public class MappingRequest {
    @JsonProperty("params")
    public HashMap<String, Object> parameters = new HashMap<>();

    @JsonProperty("subject")
    public MappingSubject mappingSubject;

    public MappingRequest() {

    }

    public MappingRequest(MappingSubject subject) {
        this.mappingSubject = subject;
    }

    public MappingRequest(MappingSubject subject, HashMap<String, Object> parameters) {
        this.mappingSubject = subject;
        this.parameters = parameters;
    }

    public MappingRequest(SchemaType schema, FeatureCollection collection) {
        this.mappingSubject = new MappingSubject(schema, collection);
    }

    public MappingRequest(SchemaType schema, FeatureCollection collection, HashMap<String, Object> parameters) {
        this.mappingSubject = new MappingSubject(schema, collection);
        this.parameters = parameters;
    }
}
