package edu.illinois.ncsa.incore.service.project.utils;

import java.util.Comparator;
import edu.illinois.ncsa.incore.service.project.models.Project;

public class CommonUtil {
    // Method to get the comparator based on sortBy and order
    public static <T> Comparator<T> projectComparator(String sortBy, String order) {
        Comparator<T> comparator;

        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(resource -> ((Project) resource).getName());
                break;
            case "creator":
                comparator = Comparator.comparing(resource -> ((Project) resource).getCreator());
                break;
            case "date":
            default:
                comparator = Comparator.comparing(resource -> ((Project) resource).getDate());
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

}
