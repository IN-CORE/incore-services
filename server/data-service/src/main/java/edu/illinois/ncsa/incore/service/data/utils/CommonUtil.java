package edu.illinois.ncsa.incore.service.data.utils;

import edu.illinois.ncsa.incore.service.data.models.Dataset;

import java.util.Comparator;

public class CommonUtil {
    public static Comparator<Dataset> datasetComparator(String sortBy, String order){
        // construct comparator
        Comparator<Dataset> comparator;
        if (sortBy.equals("title")) {
            comparator = Comparator.comparing(Dataset::getTitle);
        }
        else{
            // default to date
            comparator = Comparator.comparing(Dataset::getDate);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }
}
