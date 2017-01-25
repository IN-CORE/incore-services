package edu.illinois.ncsa.incore.repo;

import edu.illinois.ncsa.incore.repo.model.SurveyInventoryItem;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/surveyinventory")
@Produces("application/json")
public class SurveyInventoryService {
    // The Java method will process HTTP GET requests
    @GET
    public SurveyInventoryItem getSample() {
        SurveyInventoryItem sample = new SurveyInventoryItem();
//        sample.transverseDuctility = "uber";
        return sample;
    }

    //routes we need:
    // get by geo area
    // get by id
    // post
    // get by attribute query
}
