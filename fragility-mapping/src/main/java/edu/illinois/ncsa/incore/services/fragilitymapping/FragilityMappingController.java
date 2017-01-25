package edu.illinois.ncsa.incore.services.fragilitymapping;

import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.util.XmlUtils;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfRow;

import javax.ws.rs.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

@Path("mapping")
public class FragilityMappingController {

    @GET
    @Produces("application/json")
    @Path("/{mappingsetId}/{datasetId}")
    public Map<String, String> getMappings(@PathParam("mappingsetId") String mappingsetId, @PathParam("datasetId") String datasetId) {

        FragilityMapper fragilityMapper = new FragilityMapper();
        fragilityMapper.addMappingSet(mappingsetId);
        //first load the dataset based on the url


        HashMap<String, String> result = new HashMap<>();
        result.put("1", "2");
        result.put("3", "4");
        return result;
    }

    @GET
    @Produces("application/json")
    @Path("/byurl")
    public Map<String, String> getMappings(@QueryParam("mappingUrl") String mappingUrl, @QueryParam("datasetUrl") String datasetUrl, @QueryParam("options") String optionsJson) {

        //prep the mapping dataset and the mapper class
        MatchFilterMap matchFilterMap = loadMatchFilterMapFromUrl(mappingUrl);
        if (matchFilterMap == null) {
            return null;
        }
        final FragilityMapper mapper = new FragilityMapper();
        mapper.addMappingSet(matchFilterMap);

        //get the dbf for the inventory
        File localCopy = downloadFromUrl(datasetUrl);
        if (localCopy == null) {
            return null;
        }

        //prepare the params
        HashMap<String,Object> params = null;
//        try {
//            params = new ObjectMapper().readValue(optionsJson, HashMap.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }

        Map<String,String> result = new HashMap<>();
        //iterate through the silly thing and map each row
        DbfReader reader = new DbfReader(localCopy);
        int fieldsCount = reader.getHeader().getFieldsCount();
        DbfRow row;
        Map<String,Object> inventoryRow = new HashMap<>();
        while ((row = reader.nextRow()) != null) {
            inventoryRow.clear();
            for (int i = 0; i < fieldsCount; i++) {
                DbfField field = reader.getHeader().getField(i);
                String fieldName = field.getName();

                Object value = row.getObject(field.getName());
                if (value instanceof byte[]) {
                    value = row.getString(field.getName());
                }
                inventoryRow.put(fieldName, value);
            }
            String fragilityFor = mapper.getFragilityFor("", inventoryRow, params);
            result.put(String.valueOf(row.getDouble("id")), fragilityFor);
        }
//        mapper.getFragilityFor("",  )

        return result;

    }

    private File downloadFromUrl(String datasetUrl) {
        URL website = null;
        try {
            website = new URL(datasetUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            File result = File.createTempFile("tmp", ".dbf");
            FileOutputStream fos = new FileOutputStream(result);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return result;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private MatchFilterMap loadMatchFilterMapFromUrl(String mappingUrl) {
        try {
            MappingDatasetStub stub = new MappingDatasetStub();
            XmlUtils.deserializeUserFacingBeanFromFile(new URL(mappingUrl), stub);
            return stub.getMatchFilterMap();
        } catch (DeserializationException | MalformedURLException e) {
            e.printStackTrace();
        }
        return null;

    }


}
