package edu.illinois.ncsa.incore.service.dfr3.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.common.SemanticsConstants;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.dfr3.models.DFR3Set;
import jakarta.ws.rs.core.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static void extractColumnsFromMapping(Map<?, ?> rules, Set<String> columnSet) {
        rules.forEach((key, value) -> {
            if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof String) {
                        String[] parts = ((String) item).split(" ");
                        if (parts.length > 1) {
                            columnSet.add(parts[1]);
                        }
                    } else if (item instanceof Map) {
                        extractColumnsFromMapping((Map<?, ?>) item, columnSet);
                    }
                }
            }
        });
    }

    public static void extractColumnsFromMapping(ArrayList<?> rules, Set<String> columnSet) {
        rules.forEach((rule) -> {
            if (rule instanceof List) {
                ((List<?>) rule).forEach((r) -> {
                    String[] parts = r.toString().split(" ");
                    if (parts.length > 1) {
                        columnSet.add(parts[1]);
                    }
                });
            }
        });
    }
}
