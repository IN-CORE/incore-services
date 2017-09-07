package edu.illinois.ncsa.incore.services.fragility;

import edu.illinois.ncsa.incore.services.fragility.dto.MappingRequest;
import edu.illinois.ncsa.incore.services.fragility.mapping.FragilityMapper;
import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import org.apache.log4j.Logger;
import org.geojson.Feature;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("fragility")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    // TODO replace static with dependency injection
    public static MatchFilterMap matchFilterMap;

    @POST
    @Path("/select")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapFragilities(MappingRequest mappingRequest) {
        Map<String, String> fragilityMap = new HashMap<>();

        FragilityMapper mapper = new FragilityMapper();
        mapper.addMappingSet(matchFilterMap);

        for (Feature feature : mappingRequest.mappingSubject.inventory.getFeatures()) {
            String fragility = mapper.getFragilityFor(mappingRequest.mappingSubject.schemaType.toString(), feature.getProperties(), mappingRequest.parameters);
            fragilityMap.put(feature.getId(), fragility);
        }

        MappingResponse response = new MappingResponse(fragilityMap);

        return Response.ok(response)
                       .build();
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
