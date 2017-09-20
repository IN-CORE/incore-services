package edu.illinois.ncsa.incore.services.maestro.controllers;

import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("analysis")
public class AnalysesController {

    private static final Logger logger = Logger.getLogger(AnalysesController.class);

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnalyses() {
        List<Analysis> analyses = repository.getAllAnalyses();

        return Response.ok(analyses)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
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
    public Response registerNewAnalysis(Analysis analysis) {

        Analysis output = repository.addAnalysis(analysis);

        return Response.ok(output)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
    }

}
