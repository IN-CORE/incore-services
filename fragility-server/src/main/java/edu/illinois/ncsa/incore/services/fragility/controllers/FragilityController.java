package edu.illinois.ncsa.incore.services.fragility.controllers;

import edu.illinois.ncsa.incore.services.fragility.dto.MappingResponse;
import edu.illinois.ncsa.incore.services.fragility.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.fragility.dto.MappingRequest;
import edu.illinois.ncsa.incore.services.fragility.mapping.FragilityMapper;
import edu.illinois.ncsa.incore.services.fragility.mapping.MatchFilterMap;
import edu.illinois.ncsa.incore.services.fragility.model.FragilitySet;
import org.apache.log4j.Logger;
import org.geojson.Feature;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("fragility")
public class FragilityController {
    private static final Logger logger = Logger.getLogger(FragilityController.class);

    @Inject
    public MatchFilterMap matchFilterMap;

    @Inject
    private IRepository repository;

    @POST
    @Path("/select")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapFragilities(MappingRequest mappingRequest) {
        // load all available fragilities
        // TODO should filter based on schema (e.g. building, bridge, etc.) and any additional criteria
        List<FragilitySet> fragilitySets = repository.getFragilities();

        Map<String, FragilitySet> fragilitySetMap = new HashMap<>();
        Map<String, String> fragilityMap = new HashMap<>();

        FragilityMapper mapper = new FragilityMapper();

        mapper.addMappingSet(matchFilterMap);

        for (Feature feature : mappingRequest.mappingSubject.inventory.getFeatures()) {
            String fragilityKey = mapper.getFragilityFor(mappingRequest.mappingSubject.schemaType.toString(), feature.getProperties(), mappingRequest.parameters);

            Optional<FragilitySet> fragilityMatch = fragilitySets.stream()
                                                                 .filter(set -> set.getLegacyId().equals(fragilityKey))
                                                                 .findFirst();

            if(fragilityMatch.isPresent()) {
                FragilitySet fragilitySet = fragilityMatch.get();
                fragilitySetMap.put(fragilitySet.getLegacyId(), fragilitySet);
                fragilityMap.put(feature.getId(), fragilitySet.getLegacyId());
            }
        }

        // Construct response
        MappingResponse mappingResponse = new MappingResponse(fragilitySetMap, fragilityMap);

        return Response.ok(mappingResponse)
                       .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilities() {
        Datastore datastore = this.repository.getDataStore();

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
        Datastore datastore = this.repository.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .field("authors")
                                           .contains(author)
                                           .asList();

        if (sets.size() > 0) {
            GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets) {};

            return Response.ok(genericSets)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            // 404
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Response getFragilityByAttributeType(String attributeType, String attributeValue) {
        Datastore datastore = this.repository.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .filter(attributeType, attributeValue)
                                           .asList();

        if (sets.size() > 0) {
            GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets) {};

            return Response.ok(genericSets)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            // 404
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
