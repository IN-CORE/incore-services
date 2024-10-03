package edu.illinois.ncsa.incore.service.project.utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.project.models.DFR3MappingResource;
import edu.illinois.ncsa.incore.service.project.models.DatasetResource;
import edu.illinois.ncsa.incore.service.project.models.HazardResource;
import edu.illinois.ncsa.incore.service.project.models.WorkflowResource;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversionUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert List of JSON strings to List of HazardResource objects
    public static List<HazardResource> convertToHazardResources(List<String> jsonStrings) {
        List<HazardResource> hazardResources = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                HazardResource hazardResource = new HazardResource();
                hazardResource.setId((String) jsonMap.get("id"));
//                hazardResource.setStatus(HazardResource.Status.valueOf((String) jsonMap.get("status")));
                if (jsonMap.get("type") == null) {
                    throw new IllegalArgumentException("Type cannot be null");
                } else {
                    hazardResource.setType(HazardResource.Type.valueOf((String) jsonMap.get("type")));
                }
                hazardResources.add(hazardResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return hazardResources;
    }

    // Convert List of JSON strings to List of DatasetResource objects
    public static List<DatasetResource> convertToDatasetResources(List<String> jsonStrings) {
        List<DatasetResource> datasetResources = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                DatasetResource datasetResource = new DatasetResource();
                datasetResource.setId((String) jsonMap.get("id"));
//                datasetResource.setStatus(DatasetResource.Status.valueOf((String) jsonMap.get("status")));
                datasetResource.setType((String) jsonMap.get("type"));
                datasetResources.add(datasetResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return datasetResources;
    }

    // Convert List of JSON strings to List of WorkflowResource objects
    public static List<WorkflowResource> convertToWorkflowResources(List<String> jsonStrings) {
        List<WorkflowResource> workflowResources = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                WorkflowResource workflowResource = new WorkflowResource();
                workflowResource.setId((String) jsonMap.get("id"));
//                workflowResource.setStatus(WorkflowResource.Status.valueOf((String) jsonMap.get("status")));
                if (jsonMap.get("type") == null) {
                    throw new IllegalArgumentException("Type cannot be null");
                } else {
                    workflowResource.setType(WorkflowResource.Type.valueOf((String) jsonMap.get("type")));
                }
                workflowResources.add(workflowResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return workflowResources;
    }

    // Convert List of JSON strings to List of dfr3mapping objects
    public static List<DFR3MappingResource> convertToDFR3MappingResources(List<String> jsonStrings) {
        List<DFR3MappingResource> dfr3MappingResources = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
                DFR3MappingResource dfr3MappingResource = new DFR3MappingResource();
                dfr3MappingResource.setId((String) jsonMap.get("id"));
//                dfr3MappingResource.setStatus(DFR3MappingResource.Status.valueOf((String) jsonMap.get("status")));
                dfr3MappingResource.setType(DFR3MappingResource.Type.valueOf((String) jsonMap.get("type")));
                dfr3MappingResources.add(dfr3MappingResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return dfr3MappingResources;
    }

}
