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
    //http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0$Shelby_County_RES31224702005658$converted$all_bldgs_ver5_WGS1984
    @GET
    @Path("{datasetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDatasetById(@PathParam("datasetId") String id ) {
        Object dataset = loadDataFromRepository(id);
        return formatAsGeoJson(dataset);
    }

    private String formatAsGeoJson(Object dataset) {
        //TODO: this should return the data in geoJSON format
        return "{\"foo\": \"bar\"}";
    }

    private Object loadDataFromRepository(String id) {
        String urlPart = id.replace("$", "/");
        String shapefileDatasetUrl = "https://earthquake.ncsa.illinois.edu/ergo-repo/datasets/" + urlPart;

        //TODO: this should load a shapefile from a shapefileDatasetUrl and return some sort of geotools object that represents it (featuredataset or something?)
        return new Object(){
            public String foo = "bar";
        };
    }
}
