package edu.illinois.ncsa.incore.service.data.test;

/**
 * Created by ywkim on 8/7/2017.
 */
//import edu.illinois.ncsa.incore.services.fragilitymapping.FragilityMappingController;
import edu.illinois.ncsa.incore.service.data.controllers.DatasetController;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;


public class DataControllerTest extends JerseyTest {
    /**
     * Boilerplate to configure the resource controller to test
     * @return
     */
    @Override
    public Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new ResourceConfig(DatasetController.class);
    }

//    @Test
//    public void testSimpleMapping() throws UnsupportedEncodingException {
//        String output = target("/datasets/query").
//                queryParam("type", "edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0").
//                request().accept(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).get(String.class);
//        System.out.println("----------output is:" + output);
//        JSONArray parsedArray = new JSONArray(output);
//        System.out.println("hold");
//        JSONObject parsed = parsedArray.getJSONObject(0);
//        Object name = parsed.get("name");
//        assertEquals("Hospitals", name);
//
//        String input = "{\"age\":45,\"name\":\"jong lee\"}";
//        String output1 = target("/datasets/query").
//                queryParam("type", "edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0").
//                request().accept(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).post(String.class);
//        target("/insgest-result").request().post(input); //Here we send POST request
//        Response response = target("users/find").queryParam("email", "user2@mail.com").request().get(); //Here we send GET request for retrieving results
//        Assert.assertEquals("user2@mail.com", response.readEntity(User.class).getEmail())
//
//        // test ingestion part
//        try {
//            URL url = new URL("http://localhost:8080/repo/api/ingest-result");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//
////            String input = "{\"age\":45,\"name\":\"jong lee\"}";
//            String input = "{" +
//                    "   schema: \"buildingDamage\"," +
//                    "   sourceDataset: \"{id}\"," +
//                    "   collections: [" +
//                    "      //an optional list of collections to add to," +
//                    "      //we can omit this for the prototype, and only" +
//                    "      //add to a user's private collection" +
//                    "   ]," +
//                    "   analysisId: \"{datawolf workflow url of the anlysis}\"," +
//                    "   resultData: {" +
//                    "      0: {none: 0, low: 0.34, mod: 0.5, high: 0.8, meanDmg: 56.3}," +
//                    "      23: {none: 0, low: 0.34, mod: 0.5, high: 0.8, meanDmg: 56.3}," +
//                    "      43: {none: 0, low: 0.34, mod: 0.5, high: 0.8, meanDmg: 56.3}," +
//                    "   }" +
//                    "}";
//            OutputStream os = conn.getOutputStream();
//            os.write(input.getBytes());
//            os.flush();
//
//            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + conn.getResponseCode());
//            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    (conn.getInputStream())));
//
//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null) {
//                System.out.println(output);
//            }
//
//            conn.disconnect();
//
//        } catch (MalformedURLException e) {
//
//            e.printStackTrace();
//
//        } catch (IOException e) {
//
//            e.printStackTrace();
//
//        }
//    }
}
