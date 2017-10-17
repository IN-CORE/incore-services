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
import edu.illinois.ncsa.incore.service.maestro.dao.IRepository;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("analysis")
public class AnalysesController {

    private static final Logger logger = Logger.getLogger(AnalysesController.class);

    @Inject
    private IRepository repository;

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Analysis> getAnalyses() {
        List<Analysis> analyses = repository.getAllAnalyses();

        return analyses;
    }

    @GET
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnalysisById(@PathParam("analysisId") String id) {
        Analysis analysis = repository.getAnalysisById(id);
        if(analysis != null) {
            return Response.ok(analysis)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .build();
        } else {
            return Response.status(404).build();
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
