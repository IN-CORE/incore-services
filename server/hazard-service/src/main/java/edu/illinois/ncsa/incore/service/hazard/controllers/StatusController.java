package edu.illinois.ncsa.incore.service.hazard.controllers;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.dao.*;
import edu.illinois.ncsa.incore.service.hazard.utils.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "status", authorizations = {})

@Path("status")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class StatusController {
    private static final Logger logger = Logger.getLogger(StatusController.class);

    @Inject
    private IEarthquakeRepository earthquakeRepository;

    @Inject
    private IFloodRepository floodRepository;

    @Inject
    private IHurricaneRepository hurricaneRepository;

    @Inject
    private IHurricaneWindfieldsRepository hurricaneWindfieldRepository;

    @Inject
    private ITornadoRepository tornadoRepository;

    @Inject
    private ITsunamiRepository tsunamiRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gives the status of the service.",
        notes = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String statusJson = "{\"status\": \"responding\"}";
        return statusJson;
    }

    @GET
    @Path("usage")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gives the count for each hazard.", notes = "")
    public String getUserStatusHazards(@HeaderParam("x-auth-userinfo") String userInfo) {
        int numEarthquakes = 0;
        int numFloods = 0;
        int numHurricanes = 0;
        int numHurricaneWindfields = 0;
        int numTornadoes = 0;
        int numTsunamis = 0;

        String creator = "";
        JSONObject earthquakeJson = null;
        JSONObject floodJson = null;
        JSONObject hurricaneJson = null;
        JSONObject hurricaneWindfieldsJson = null;
        JSONObject tornadoJson = null;
        JSONObject tsunamiJson = null;

        JSONArray hazardJson = new JSONArray();

        try {
            JSONParser parser = new JSONParser();
            JSONObject userInfoJson = (JSONObject) parser.parse(userInfo);
            creator = (String) userInfoJson.get("preferred_username");
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Invalid User Info!");
        }

        try {
            numEarthquakes = earthquakeRepository.getEarthquakesCountByCreator(creator);
            earthquakeJson = CommonUtil.createUserStatusJson(creator, "earthquakes", numEarthquakes);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get earthquake status!");
        }

        try {
            numFloods = floodRepository.getFloodsCountByCreator(creator);
            floodJson = CommonUtil.createUserStatusJson(creator, "floods", numFloods);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get flood status!");
        }

        try {
            numHurricanes = hurricaneRepository.getHurricanesCountByCreator(creator);
            hurricaneJson = CommonUtil.createUserStatusJson(creator, "hurricanes", numHurricanes);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get hurricane status!");
        }

        try {
            numHurricaneWindfields = hurricaneWindfieldRepository.getHurricaneWindfieldsCountByCreator(creator);
            hurricaneWindfieldsJson = CommonUtil.createUserStatusJson(creator, "hurricaneWindfields", numHurricaneWindfields);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get hurricaneWindfield status!");
        }

        try {
            numTornadoes = tornadoRepository.getTornadoesCountByCreator(creator);
            tornadoJson = CommonUtil.createUserStatusJson(creator, "tornadoes", numTornadoes);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get tornado status!");
        }

        try {
            numTsunamis = tsunamiRepository.getTsunamisCountByCreator(creator);
            tsunamiJson = CommonUtil.createUserStatusJson(creator, "tsunamis", numTsunamis);
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get tsunami status!");
        }

        hazardJson.add(earthquakeJson);
        hazardJson.add(floodJson);
        hazardJson.add(hurricaneJson);
        hazardJson.add(hurricaneWindfieldsJson);
        hazardJson.add(tornadoJson);
        hazardJson.add(tsunamiJson);

        return hazardJson.toString();
    }

}
