/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.illinois.ncsa.incore.service.dfr3.models.MappingSubject;
import edu.illinois.ncsa.incore.service.dfr3.models.SchemaType;
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
