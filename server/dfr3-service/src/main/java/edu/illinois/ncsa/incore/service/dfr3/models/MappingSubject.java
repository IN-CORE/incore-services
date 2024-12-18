/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.dfr3.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Embedded;
import org.geojson.GeoJsonObject;

@Embedded
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
