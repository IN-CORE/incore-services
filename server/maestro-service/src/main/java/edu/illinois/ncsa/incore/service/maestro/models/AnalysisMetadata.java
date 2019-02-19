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

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;

/*
In some cases, we want to be able to return relevant information for an analysis but not including the
datasets, parameters and outputs. Using this class and extends in the Analysis class allows for returning
from the same method either AnalysisMetadata or Analysis depending on the full query parameter. 
 */
@XmlRootElement
public class AnalysisMetadata {
    @Property("_id")
    private ObjectId id;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setHelpContext(String helpContext) {
        this.helpContext = helpContext;
    }

    private String description;
    private String name;
    private String url;
    private String category;
    private String helpContext;

    public AnalysisMetadata() {}

    public AnalysisMetadata(ObjectId id, String name, String description, String category, String url, String helpContext){
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.category = category;
        this.helpContext = helpContext;
    }

    public String getId() {
        return id.toString();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }

    public String getHelpContext() {
        return helpContext;
    }

}
