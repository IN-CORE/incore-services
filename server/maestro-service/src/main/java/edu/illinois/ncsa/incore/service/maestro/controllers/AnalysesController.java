/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.controllers;

import edu.illinois.ncsa.incore.service.maestro.models.Analysis;
import edu.illinois.ncsa.incore.service.maestro.models.AnalysisMetadata;
import edu.illinois.ncsa.incore.service.maestro.daos.IRepository;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("analyses")
public class AnalysesController {

    private static final Logger logger = Logger.getLogger(AnalysesController.class);

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Analysis> getAnalyses() {
        List<Analysis> analyses = repository.getAllAnalyses();

        return analyses;
    }

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AnalysisMetadata> getAnalysesMetadata() {
        List<Analysis> analyses = repository.getAllAnalyses();
        List<AnalysisMetadata> metadata = new ArrayList<AnalysisMetadata>();
        for(int i = 0; i < analyses.size(); i++) {
            metadata.add(analyses.get(i).getMetadata());
        }

        return metadata;
    }

    @GET
    @Path("{analysisId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Analysis getAnalysisById(@PathParam("analysisId") String id) {
        Analysis analysis = repository.getAnalysisById(id);
        if(analysis != null) {
            return analysis;
        } else {
            throw new NotFoundException("No analysis with id: "+ id);
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Analysis registerNewAnalysis(Analysis analysis) {

        Analysis output = repository.addAnalysis(analysis);

        return output;
    }

}
