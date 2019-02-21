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

public class AnalysisParameter {

    private String name;
    private String description;
    private Boolean required;
    private Boolean advanced;
    private Boolean multiple;
    private Boolean hidden;
    private String type;

    public AnalysisParameter() {}

    public AnalysisParameter(String name, String description, String type, Boolean required, Boolean advanced,
                             Boolean hidden, Boolean multiple){
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.advanced = advanced;
        this.multiple = multiple;
        this.hidden = hidden;
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

    public String getType() {
        return type;
    }

    public Boolean getHidden() {
        return hidden;
    }
}
