/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.models;

public class AnalysisDataset {

    private String name;
    private String description;
    private Boolean required;
    private Boolean advanced;
    private Boolean multiple;
    private String[] type;

    public AnalysisDataset() {}

    public AnalysisDataset(String name, String description, String[] type, Boolean required, Boolean advanced, Boolean multiple){
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.advanced = advanced;
        this.multiple = multiple;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getRequired() {
        return required;
    }

    public Boolean getAdvanced() {
        return advanced;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public String[] getType() {
        return type;
    }
}
