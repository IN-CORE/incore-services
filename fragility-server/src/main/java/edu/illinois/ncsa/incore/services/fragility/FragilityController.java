package edu.illinois.ncsa.incore.services.fragility;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.services.fragility.dto.MappingRequest;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("fragility")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    @POST
    @Path("/select")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapFragilities(MappingRequest mappingRequest) {
        return Response.status(500).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilities() {
        Datastore datastore = DataAccess.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .limit(100)
                                           .asList();

        GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets) {};

        return Response.ok(genericSets)
                       .header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Methods", "GET")
                       .build();
    }

    @GET
    @Path("{fragilityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilityById(@PathParam("fragilityId") String id) throws Exception {
        return getFragilityByAttributeType("_id", id);
    }

    @GET
    @Path("/demand/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilityByDemandType(@PathParam("type") String type) throws Exception {
        return getFragilityByAttributeType("demandType", type);
    }

    @GET
    @Path("/hazard/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilityByHazardType(@PathParam("type") String type) throws Exception {
        return getFragilityByAttributeType("hazardType", type);
    }

    @GET
    @Path("/inventory/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilityByInventoryType(@PathParam("type") String type) throws Exception {
        return getFragilityByAttributeType("inventoryType", type);
    }

    @GET
    @Path("/author/{author}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilityByAuthor(@PathParam("author") String author) throws Exception {
        Datastore datastore = DataAccess.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .field("authors").contains(author)
                                           .asList();

        if (sets.size() > 0) {
            GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets) {};

            return Response.ok(genericSets)
                           .status(200)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }

    private Response getFragilityByAttributeType(String attributeType, String attributeValue) {
        Datastore datastore = DataAccess.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .filter(attributeType, attributeValue)
                                           .asList();

        if (sets.size() > 0) {
            GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets) {};

            return Response.ok(genericSets)
                           .status(200)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }
}
