package edu.illinois.ncsa.incore.service.dfr3.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.dfr3.daos.IFragilityDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IMappingDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRepairDAO;
import edu.illinois.ncsa.incore.service.dfr3.daos.IRestorationDAO;
import edu.illinois.ncsa.incore.service.dfr3.utils.CommonUtil;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import jakarta.inject.Inject;
// import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag(name = "status")

@Path("status")
@ApiResponses(value = {
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class StatusController {
    // @Context
    // private HttpServletRequest context;
    private static final Logger logger = Logger.getLogger(StatusController.class);
    private final HashMap<String, String> tests = new HashMap<String, String>();
    private static final String VERSION = System.getenv("SERVICE_VERSION");

    public StatusController() {
        tests.put("dbconn", "unknown");
    }

    @Inject
    private IFragilityDAO fragilityRepository;

    @Inject
    private IMappingDAO mappingRepository;

    @Inject
    private IRepairDAO repairRepository;

    @Inject
    private IRestorationDAO restorationRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(tags = "Gives the status of the service.",
        description = "This will provide the status of the service as a JSON.")
    public String getStatus() {
        String time = OffsetDateTime.now(ZoneOffset.UTC).toString();
        String errorRE = "(?i).*incoreserviceerror.*";
        Pattern errorPat = Pattern.compile(errorRE);
        String messageRE = "(?i).*message\"?\\s*:[^\"]*\"([^\"]*)\".*";
        Pattern messagePat = Pattern.compile(messageRE);
        String status = "responding";
        String statusJson = "{\"time\":\"" + time + "\",\"specificTests\":{";
        for (Map.Entry<String, String> test : tests.entrySet()) {
            String key = test.getKey();
            String value = test.getValue();
            int intValue = 0;
            String testIntValue = "";
            if (key == "dbconn") {
                value = testDB();
            }
            if (value.charAt(0) != '{' && value.charAt(0) != '[') {
                try {
                    if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                        testIntValue = value.substring(1, value.length() - 1);
                    }
                    intValue = Integer.parseInt(testIntValue);
                    value = Integer.toString(intValue);
                } catch (NumberFormatException e) {
                    value = "\"" + value + "\"";
                }
            }
            statusJson += '"' + key + "\":" + value + ",";
            Matcher messageMatch = messagePat.matcher(value);
            if (messageMatch.find()) {
                Matcher errorMatch = errorPat.matcher(messageMatch.group(0));
                if (errorMatch.find()) {
                    status = "IncoreServiceError: Specific tests have at least one error message";
                }
            } else {
                String codeCheckRE = "(?i).*code:[^:]*(-?[0-9]+).*,";
                Pattern codeCheckPat = Pattern.compile(codeCheckRE);
                Matcher codeCheckMatch = codeCheckPat.matcher(value);
                if (codeCheckMatch.find()) {
                    if (Integer.parseInt(codeCheckMatch.group(0)) > 0) {
                        status = "IncoreServiceError: Specific tests have at least one code greater than 0";
                    }
                } else {
                    //No code returned for a check is an error for the service
                    status = "IncoreServiceError: Specific tests have at least one no code returned";
                }
            }
        }
        //Trim the final , before closing out the dictionary
        statusJson = statusJson.substring(0, statusJson.length() - 1);
        String code = "0";
        Matcher errorMatch = errorPat.matcher(status);
        if (errorMatch.find()) {
            code = "1";
        }
        statusJson += "},\"status\":\"" + status + "\",\"code\":" + code + ", \"version\":\"" + VERSION + "\"}";
        return statusJson;
    }

    public String testDB() {
        long startTime = System.nanoTime();
        HashMap<String, String> retHash = new HashMap<String, String>();
        int connTimeout = 3000;
        int connTimeoutProp = 0;
        try {
            connTimeoutProp = Integer.parseInt(System.getenv("MONGO_STATUS_CONN_TIMEOUT"));
        } catch (NumberFormatException e) {
            connTimeoutProp = 0;
        }
        if (connTimeoutProp > 0) {
            //This allows the environment variable to be set in seconds, though we need ms
            connTimeout = connTimeoutProp * 1000;
        }
        String mongodbUri = "mongodb://localhost:27017/dfr3db?maxpoolsize=100";

        String mongodbUriProp = System.getenv("DFR3_MONGODB_URI");
        if (mongodbUriProp != null && !mongodbUriProp.isEmpty()) {
            mongodbUri = mongodbUriProp;
        }
        // Add connection timeouts to the uri string to force a quick timeout
        if (mongodbUri.matches(".*\\?[^/]*$")) {
            mongodbUri += "&";
        } else {
            mongodbUri += "?";
        }
        mongodbUri += "connectTimeoutMS=" + connTimeout + "&socketTimeoutMS=" + connTimeout +
            "&serverSelectionTimeoutMS=" + connTimeout;
        MongoClientURI mongoURI = new MongoClientURI(mongodbUri);

        try {
            MongoClient testCon = new MongoClient(mongoURI);
            MongoCursor<String> dbs = testCon.listDatabaseNames().iterator();
            retHash.put("code", "0");
            retHash.put("message", "Database connection successful.");
        } catch (Exception e) {
            //the connection failed
            retHash.put("code", "1");
            retHash.put("message", "IncoreServiceError: Database connection failed, " + e);
        }

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        retHash.put("execTime", Long.toString(elapsedTime));
        String ret = retHash.get("code");
        try {
            ret = new ObjectMapper().writeValueAsString(retHash);
        } catch (IOException e) {
            //Do nothing, ret's default value is fine
        }
        //Strip the quotes from code and execTime
        ret = ret.replaceAll("\"code\":\"(\\d+)\"", "\"code\":$1");
        ret = ret.replaceAll("\"execTime\":\"(\\d+)\"", "\"execTime\":$1");
        return ret;
    }

    @GET
    @Path("usage")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "Gives the count for each dfr3 database.", description = "")
    public String getUserStatusHazards(@HeaderParam("x-auth-userinfo") String userInfo) {
        int numFragilities = 0;
        int numMappins = 0;
        int numRepair = 0;
        int numRestoration = 0;

        String creator = "";
        JSONObject fragilityJson = null;
        JSONObject mappingJson = null;
        JSONObject repairJson = null;
        JSONObject restorationJson = null;

        JSONArray dfr3Json = new JSONArray();

        try {
            JSONParser parser = new JSONParser();
            JSONObject userInfoJson = (JSONObject) parser.parse(userInfo);
            creator = (String) userInfoJson.get("preferred_username");
        } catch (Exception e) {
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "Unable to get username!");
        }

        numFragilities = fragilityRepository.getFragilityCountByCreator(creator);
        fragilityJson = CommonUtil.createUserStatusJson(creator, "fragility", numFragilities);

        numMappins = mappingRepository.getMappingCountByCreator(creator);
        mappingJson = CommonUtil.createUserStatusJson(creator, "mapping", numMappins);

        numRepair = repairRepository.getRepairCountByCreator(creator);
        repairJson = CommonUtil.createUserStatusJson(creator, "repair", numRepair);

        numRestoration = restorationRepository.getRestorationCountByCreator(creator);
        restorationJson = CommonUtil.createUserStatusJson(creator, "restoration", numRestoration);

        dfr3Json.add(fragilityJson);
        dfr3Json.add(mappingJson);
        dfr3Json.add(repairJson);
        dfr3Json.add(restorationJson);

        return dfr3Json.toString();
    }
}
