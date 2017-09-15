package edu.illinois.ncsa.incore.services.maestro.controllers;

import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.dto.AnalysisRequest;
import edu.illinois.ncsa.incore.services.maestro.model.Analysis;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
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
    public Response registerNewAnalysis(AnalysisRequest analysisRequest) {

        Analysis analysis = new Analysis(analysisRequest.parameters.get("name").toString(),
            analysisRequest.parameters.get("category").toString(),
            analysisRequest.parameters.get("description").toString(),
            analysisRequest.parameters.get("url").toString(),
            Collections.emptyList(), Collections.emptyList());
//            Collections.list(analysisRequest.parameters.get("inputs")),
//            analysisRequest.parameters.get("outputs"));
        String output = repository.addAnalysis(analysis);

        return Response.ok(output)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
    }

}
