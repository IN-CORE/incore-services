/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/

package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import edu.illinois.ncsa.incore.semantic.metamodel.common.reference.Reference;

import java.util.ArrayList;
import java.util.List;

public abstract class Concept<T> {
    public String namespace;
    public String resourceName;
    // 10 character resource name
    public String resourceNameShort;
    public List<String> aliases = new ArrayList<>();
    public String description;

    // Backup Cache?
    public Reference reference;

    // Category?
    // Category Type?
    // Reference?

    // Future
    // public String descriptionHtml;
    // public String notes;

    // List<Dataset> OwnerDatasets?

    // related fields
    // foreign key

    // *Validation*

    // *Statistics*
    // Typical Range if Number
    // Typical Length?
    // Mean Value

    public boolean typicallyNullable = true;
    public boolean typicallyUnique = false;

    // region Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getResourceNameShort() {
        return resourceNameShort;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTypicallyNullable() {
        return typicallyNullable;
    }

    public boolean isTypicallyUnique() {
        return typicallyUnique;
    }
    // endregion
}
