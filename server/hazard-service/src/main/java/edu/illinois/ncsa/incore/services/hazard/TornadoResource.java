package edu.illinois.ncsa.incore.services.hazard;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tornado")
public class TornadoResource {

    @GET
    @Path("/model")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTornadoModelHazard(@QueryParam("modelId") String modelId, @QueryParam("datasetId") String tornadoId, @QueryParam("demandUnits") String demandUnits, @QueryParam("siteLat") double siteLat, @QueryParam("siteLong") double siteLong ) {

        // calculate wind speed
        return Response.ok("Return tornado model hazard for "+modelId).build();
    }

}
