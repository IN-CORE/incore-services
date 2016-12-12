package edu.illinois.ncsa.Fragility;

import javax.swing.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.illinois.ncsa.Fragility.Model.FragilitySet;
import edu.illinois.ncsa.Utils.ClientUtils;
import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;

import java.util.List;

@Path("fragility")
public class FragilityResource {
    private static final Logger logger = Logger.getLogger(FragilityResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragilities() {
        Datastore datastore = ClientUtils.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .limit(20)
                                           .asList();

        GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets){};

        return Response.ok(genericSets)
                       .header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                       .build();
    }

    @GET
    @Path("{fragilityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragility(@PathParam("fragilityId") String id) throws Exception {
        Datastore datastore = ClientUtils.getDataStore();

        List<FragilitySet> sets = datastore.createQuery(FragilitySet.class)
                                           .filter("id", id)
                                           .asList();

        GenericEntity<List<FragilitySet>> genericSets = new GenericEntity<List<FragilitySet>>(sets){};

        return Response.ok(genericSets)
                       .header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                       .build();
    }
}
