/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd
 */

package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

import java.util.HashMap;
import java.util.Map;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mapping {
    public final Map<String, String> legacyEntry = new HashMap<>();
    public final Map<String, String> entry = new HashMap<>();
}
