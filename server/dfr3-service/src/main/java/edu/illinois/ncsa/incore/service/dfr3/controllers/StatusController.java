package edu.illinois.ncsa.incore.service.dfr3.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(value = "status", authorizations = {})

@Path("status")
@ApiResponses(value = {
    @ApiResponse(code = 500, message = "Internal Server Error")
})
public class StatusController {
    @Context
    private HttpServletRequest context;
    private static final Logger logger = Logger.getLogger(StatusController.class);
    private HashMap<String, String> tests = new HashMap<String, String>();
    private String user = "incrtest";
    private int period = 5;
    private static final String VERSION = System.getenv("SERVICE_VERSION");

    public StatusController() {
        tests.put("dbconn", "unknown");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Gives the status of the service.",
        notes = "This will provide the status of the service as a JSON.")
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
}
