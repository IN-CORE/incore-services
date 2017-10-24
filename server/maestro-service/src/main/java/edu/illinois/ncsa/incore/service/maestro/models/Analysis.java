/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
public class Analysis {

    @Id
    @Property("_id")
    private ObjectId id;

    private String description;
    private String name;
    private String url;
    private String category;
    private String helpContext;
    private String tag;
    private List<AnalysisDataset> datasets;
    private List<AnalysisParameter> parameters;
    private List<AnalysisOutput> outputs;
    private AnalysisMetadata metadata;


    public Analysis() {}

    public Analysis(String name, String description, String category, String url, List<AnalysisDataset> datasets,
                    List<AnalysisOutput> outputs, String tag, String helpContext, List<AnalysisParameter> parameters){
        this.name = name;
        this.description = description;
        this.url = url;
        this.datasets = datasets;
        this.outputs = outputs;
        this.category = category;
        this.tag = tag;
        this.helpContext = helpContext;
        this.parameters = parameters;
    }

    public AnalysisMetadata getMetadata() {
        if(metadata == null) {
            metadata = new AnalysisMetadata(this.id, this.name, this.description, this.category, this.url, this.helpContext);
        }
        return metadata;
    }


    public String getId() {
        return id.toString();
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public List<AnalysisDataset> getDatasets() {
        return datasets;
    }

    public List<AnalysisOutput> getOutputs() {
        return outputs;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getHelpContext() {
        return helpContext;
    }

    public String getTag() {
        return tag;
    }

    public List<AnalysisParameter> getParameters() {
        return parameters;
    }
}
