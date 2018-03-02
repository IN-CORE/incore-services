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
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Path("analyses")
public class AnalysesController {

    private static final Logger logger = Logger.getLogger(AnalysesController.class);

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends AnalysisMetadata> getAnalyses(@QueryParam("category") String category,
                                      @QueryParam("name") String name,
                                      @DefaultValue("false") @QueryParam("full") Boolean full,
                                      @QueryParam("skip") int offset,
                                      @DefaultValue("100") @QueryParam("limit") int limit) {

        Map<String, String> queryMap = new HashMap<>();

        if(category != null) {
            queryMap. put("category", category);
        }

        if(name != null) {
            queryMap.put("name", name);
        }

        List<? extends AnalysisMetadata> analyses;
        if(queryMap.isEmpty() && full) {
            analyses = repository.getAllAnalyses();
        } else {
            analyses = repository.getAnalysis(queryMap, offset, limit);
            if(!full) {
                List<AnalysisMetadata> metadata = new ArrayList<>();

                for( AnalysisMetadata analysis: analyses) {
                    AnalysisMetadata metadataItem =  new AnalysisMetadata(new ObjectId(analysis.getId()),
                        analysis.getName(), analysis.getDescription(),
                        analysis.getCategory(), analysis.getUrl(), analysis.getHelpContext());
                    metadata.add(metadataItem);
                }
                analyses = metadata;
            }
        }

        return analyses;
    }


    @GET
    @Path("{analysisId}")
    @Produces({MediaType.APPLICATION_JSON})
    public AnalysisMetadata getAnalysisById(@PathParam("analysisId") String id,
                                            @DefaultValue("true") @QueryParam("full") Boolean full) {
        Analysis analysis = repository.getAnalysisById(id);
        if(analysis != null) {
            if(full) {
                return analysis;
            } else {
               AnalysisMetadata metadata = new AnalysisMetadata(new ObjectId(analysis.getId()),
                   analysis.getName(), analysis.getDescription(),
                   analysis.getCategory(), analysis.getUrl(), analysis.getHelpContext());
                return metadata;
            }

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
