/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.controllers;

import com.vividsolutions.jts.geom.GeometryFactory;
import edu.illinois.ncsa.incore.common.auth.IAuthorizer;
import edu.illinois.ncsa.incore.service.hazard.dao.IEarthquakeRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.IHurricaneRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.ITornadoRepository;
import edu.illinois.ncsa.incore.service.hazard.dao.ITsunamiRepository;
import edu.illinois.ncsa.incore.service.hazard.models.eq.*;
import edu.illinois.ncsa.incore.service.hazard.models.hurricane.HurricaneWindfields;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.Tornado;
import edu.illinois.ncsa.incore.service.hazard.models.tsunami.Tsunami;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

// @SwaggerDefinition is common for all the service's controllers and can be put in any one of them
@SwaggerDefinition(
    info = @Info(
        description = "Incore Hazard Service For Earthquake, Tornado, Tsunami and Hurricane",
        version = "v0.2.0",
        title = "Incore v2 Hazard API",
        contact = @Contact(
            name = "Jong S. Lee",
            email = "jonglee@illinois.edu",
            url = "http://resilience.colostate.edu"
        ),
        license = @License(
            name = "Mozilla Public License 2.0 (MPL 2.0)",
            url = "https://www.mozilla.org/en-US/MPL/2.0/"
        )
    ),
    consumes = {"application/json"},
    produces = {"application/json"},
    schemes = {SwaggerDefinition.Scheme.HTTP}
//    ,tags = {
//        @Tag(name = "Private", description = "Tag used to denote operations as private")
//    },
    //externalDocs = @ExternalDocs(value = "FEMA  Hazard Manual", url = "https://www.fema.gov/earthquake")
)

@Api(value = "hazards", authorizations = {})

@Path("hazards")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class HazardController {
    private static final Logger logger = Logger.getLogger(HazardController.class);
    private GeometryFactory factory = new GeometryFactory();

    @Inject
    private IEarthquakeRepository earthquakeRepository;

    @Inject
    private IHurricaneRepository hurricaneRepository;

    @Inject
    private ITornadoRepository tornadoRepository;

    @Inject
    private ITsunamiRepository tsunamiRepository;

    @Inject
    private IAuthorizer authorizer;

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Search for a text in all datasets", notes="Gets all datasets that contain a specific text")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No datasets found with the searched text")
    })
    public List<Object> findHazards(@HeaderParam("X-Credential-Username") String username,
                                      @ApiParam(value="Text to search by", example = "building") @QueryParam("text") String text,
                                      @ApiParam(value = "Skip the first n results") @QueryParam("skip") int offset,
                                      @ApiParam(value = "Limit no of results to return") @DefaultValue("100") @QueryParam("limit") int limit) {
        List<Earthquake> earthquakes = this.earthquakeRepository.searchEarthquakes(text);
        List<HurricaneWindfields> hurricanes = this.hurricaneRepository.searchHurricanes(text);
        List<Tornado> tornadoes = this.tornadoRepository.searchTornadoes(text);
        List<Tsunami> tsunamis = this.tsunamiRepository.searchTsunamis(text);

        earthquakes = earthquakes.stream()
            .filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());
        hurricanes = hurricanes.stream()
            .filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());
        tornadoes = tornadoes.stream().
            filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());
        tsunamis = tsunamis.stream().
            filter(b -> authorizer.canRead(username, b.getPrivileges()))
            .collect(Collectors.toList());

        List<Object> datasets = new ArrayList<>();
        datasets.addAll(earthquakes);
        datasets.addAll(hurricanes);
        datasets.addAll(tornadoes);
        datasets.addAll(tsunamis);

        return datasets.stream()
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());
    }

}
