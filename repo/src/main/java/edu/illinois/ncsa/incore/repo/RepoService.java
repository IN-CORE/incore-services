package edu.illinois.ncsa.incore.repo;
import com.sun.net.httpserver.HttpServer;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/datasets")
public class RepoService {
    // The Java method will process HTTP GET requests
    @GET
    @Path("{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatasetById(@PathParam("datasetId") String id ) {
        Object dataset = loadDataFromRepository(id);
        return formatAsGeoJson(dataset);
    }

    private String formatAsGeoJson(Object dataset) {
        return "{\"foo\": \"bar\"}";
    }

    private Object loadDataFromRepository(String id) {
        return new Object(){
            public String foo = "bar";
        };
    }
}
