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
import org.geojson.GeoJsonObject;

public class MappingSubject {
    @JsonProperty("schema")
    public SchemaType schemaType;

    @JsonProperty("inventory")
    public GeoJsonObject inventory;

    public MappingSubject() {

    }

    public MappingSubject(SchemaType type, GeoJsonObject inventory) {
        this.schemaType = type;
        this.inventory = inventory;
    }
}
