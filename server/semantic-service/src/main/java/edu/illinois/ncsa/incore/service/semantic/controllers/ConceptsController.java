
package edu.illinois.ncsa.incore.service.semantic.controllers;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.Concept;
import edu.illinois.ncsa.incore.semantic.metamodel.instances.Concepts;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("concepts")
public class ConceptsController {
    @GET
    @Path("/{uri}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getConcept(@PathParam("uri") String uri) {
        Optional<Concept> result = Concepts.getByName(uri);

        if (result.isPresent()) {
            Concept concept = result.get();

            return Response.ok(concept)
                           .status(200)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Path("/match")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response findMatches(@QueryParam("name") String searchTerm) {
        Optional<Concept> result = Concepts.getByNameOrAlias(searchTerm);

        if (result.isPresent()) {
            Concept concept = result.get();

            return Response.ok(concept)
                           .status(200)
                           .header("Access-Control-Allow-Origin", "*")
                           .header("Access-Control-Allow-Methods", "GET")
                           .build();
        } else {
            return Response.status(404).build();
        }
    }
}
