package edu.illinois.ncsa.incore.services.maestro.controllers;

import edu.illinois.ncsa.incore.services.maestro.dataaccess.IRepository;
import edu.illinois.ncsa.incore.services.maestro.dto.ServiceRequest;
import edu.illinois.ncsa.incore.services.maestro.model.Service;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Path("maestro")
public class ServicesController {

    private static final Logger logger = Logger.getLogger(ServicesController.class);

    @Inject
    private IRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices() {
        List<Service> services = repository.getAllServices();

        return Response.ok(services)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
    }

    @GET
    @Path("{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceById(@PathParam("serviceId") String id) {
        Service service = repository.getServiceById(id);
        if(service != null) {
            return Response.ok(service)
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
    public Response addNewService(ServiceRequest serviceRequest) {

        Service service = new Service(serviceRequest.parameters.get("name").toString(),
            serviceRequest.parameters.get("description").toString(),
            serviceRequest.parameters.get("url").toString(),
            Collections.emptyList(), Collections.emptyList());
//            Collections.list(serviceRequest.parameters.get("inputs")),
//            serviceRequest.parameters.get("outputs"));
        String output = repository.addService(service);

        return Response.ok(output)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
    }

}
