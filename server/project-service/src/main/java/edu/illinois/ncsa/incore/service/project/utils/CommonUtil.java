package edu.illinois.ncsa.incore.service.project.utils;

import java.util.Comparator;

import edu.illinois.ncsa.incore.service.project.models.*;

public class CommonUtil {
    // Method to get the comparator based on sortBy and order
    public static Comparator<Project> projectComparator(String sortBy, String order) {
        Comparator<Project> comparator;

        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(Project::getName);
                break;
            case "creator":
                comparator = Comparator.comparing(Project::getCreator);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(Project::getDate);
                break;
        }

        // If order is "desc", reverse the comparator
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public static Comparator<HazardResource> hazardComparator(String sortBy, String order) {
        Comparator<HazardResource> comparator;

        switch (sortBy.toLowerCase()) {
            case "type":
                comparator = Comparator.comparing(HazardResource::getType);
                break;
            case "id":
                comparator = Comparator.comparing(HazardResource::getId);
                break;
            case "name":
                comparator = Comparator.comparing(HazardResource::getName);
                break;
            case "creator":
                comparator = Comparator.comparing(HazardResource::getCreator);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(HazardResource::getDate);
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }


    public static Comparator<DatasetResource> datasetComparator(String sortBy, String order) {
        Comparator<DatasetResource> comparator;

        switch (sortBy.toLowerCase()) {
            case "title":
                comparator = Comparator.comparing(DatasetResource::getTitle);
                break;
            case "type":
                comparator = Comparator.comparing(DatasetResource::getDataType);
                break;
            case "id":
                comparator = Comparator.comparing(DatasetResource::getId);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(DatasetResource::getDate);
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public static Comparator<DFR3MappingResource> dfr3MappingComparator(String sortBy, String order) {
        Comparator<DFR3MappingResource> comparator;

        switch (sortBy.toLowerCase()) {
            case "hazardtype":
                comparator = Comparator.comparing(DFR3MappingResource::getHazardType, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "inventorytype":
                comparator = Comparator.comparing(DFR3MappingResource::getInventoryType, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "type":
                comparator = Comparator.comparing(DFR3MappingResource::getType);
                break;
            case "id":
                comparator = Comparator.comparing(DFR3MappingResource::getId);
                break;
            case "name":
                comparator = Comparator.comparing(DFR3MappingResource::getName);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(DFR3MappingResource::getDate);
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public static Comparator<WorkflowResource> workflowComparator(String sortBy, String order) {
        Comparator<WorkflowResource> comparator;

        switch (sortBy.toLowerCase()) {
            case "type":
                comparator = Comparator.comparing(WorkflowResource::getType);
                break;
            case "id":
                comparator = Comparator.comparing(WorkflowResource::getId);
                break;
            case "title":
                comparator = Comparator.comparing(WorkflowResource::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "created":
            default:
                comparator = Comparator.comparing(WorkflowResource::getCreated, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public static Comparator<VisualizationResource> visualizationComparator(String sortBy, String order) {
        Comparator<VisualizationResource> comparator;

        switch (sortBy.toLowerCase()) {
            case "type":
                comparator = Comparator.comparing(VisualizationResource::getType);
                break;
            case "id":
                comparator = Comparator.comparing(VisualizationResource::getId, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "name":
                comparator = Comparator.comparing(VisualizationResource::getName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "date":
            default:
                comparator = Comparator.comparing(VisualizationResource::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }


}
