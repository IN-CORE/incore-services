package edu.illinois.ncsa.incore.service.project.utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.ncsa.incore.service.project.models.*;
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
                HazardResource hazardResource = objectMapper.readValue(jsonString, HazardResource.class);
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
                DatasetResource datasetResource = objectMapper.readValue(jsonString, DatasetResource.class);
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
                WorkflowResource workflowResource = objectMapper.readValue(jsonString, WorkflowResource.class);
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
                DFR3MappingResource dfr3MappingResource = objectMapper.readValue(jsonString, DFR3MappingResource.class);
                dfr3MappingResources.add(dfr3MappingResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return dfr3MappingResources;
    }

    // Convert List of JSON strings to List of visualization objects
    public static List<VisualizationResource> convertToVisualizationResources(List<String> jsonStrings) {
        List<VisualizationResource> visualizationResources = new ArrayList<>();
        for (String jsonString : jsonStrings) {
            try {
                VisualizationResource visualizationResource = objectMapper.readValue(jsonString, VisualizationResource.class);
                visualizationResources.add(visualizationResource);
            } catch (IOException e) {
                e.printStackTrace(); // Handle or log the exception as needed
            }
        }
        return visualizationResources;
    }

}
