package edu.illinois.ncsa.incore.service.dfr3.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {
    public static JSONObject createUserStatusJson(String creator, String keyDatabase, int numDfr) {
        JSONObject outJson = new JSONObject();
        outJson.put("creator", creator);
        outJson.put("dfr3_type", keyDatabase);
        outJson.put("total_number_of_entry", numDfr);

        return outJson;
    }

    public static List<String> getColumnNames(String semanticDefinition) throws IOException {

        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(semanticDefinition);

        // Navigate to "tableSchema" -> "columns"
        JsonNode columnsNode = rootNode.path("tableSchema").path("columns");

        // Extract the "name" field from each column and add to a list
        List<String> columnNames = new ArrayList<>();
        if (columnsNode.isArray()) {
            for (JsonNode column : columnsNode) {
                String columnName = column.path("name").asText();
                columnNames.add(columnName);
            }
        }

        return columnNames;
    }
}
