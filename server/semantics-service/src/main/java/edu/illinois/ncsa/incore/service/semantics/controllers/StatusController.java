package edu.illinois.ncsa.incore.service.semantics.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@OpenAPIDefinition(
    info = @Info(
        description = "IN-CORE Semantics Services for type and data type",
        version = "1.29.0",
        title = "IN-CORE v2 Semantics Service API",
        contact = @Contact(
            name = "IN-CORE Dev Team",
            email = "incore-dev@lists.illinois.edu",
            url = "https://tools.in-core.org"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    )
)

@Tag(name = "status")

@Path("status")
public class StatusController {
    private static final Logger logger = Logger.getLogger(StatusController.class);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Gives the status of the service.",
        description = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String statusJson = "{\"status\": \"responding\"}";
        return statusJson;
    }
}
