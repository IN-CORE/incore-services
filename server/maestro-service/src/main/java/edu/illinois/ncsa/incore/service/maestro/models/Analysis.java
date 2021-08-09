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
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
public class Analysis extends AnalysisMetadata {

    private String tag;
    private List<AnalysisDataset> datasets;
    private List<AnalysisParameter> parameters;
    private List<AnalysisOutput> outputs;


    public Analysis() {
    }

    public Analysis(String name, String description, String category, String url, List<AnalysisDataset> datasets,
                    List<AnalysisOutput> outputs, String tag, String helpContext, List<AnalysisParameter> parameters) {
        super.setName(name);
        super.setDescription(description);
        super.setUrl(url);
        super.setCategory(category);
        super.setHelpContext(helpContext);
        this.datasets = datasets;
        this.outputs = outputs;
        this.tag = tag;
        this.parameters = parameters;
    }

    public List<AnalysisDataset> getDatasets() {
        return datasets;
    }

    public List<AnalysisOutput> getOutputs() {
        return outputs;
    }

    public String getTag() {
        return tag;
    }

    public List<AnalysisParameter> getParameters() {
        return parameters;
    }

}
