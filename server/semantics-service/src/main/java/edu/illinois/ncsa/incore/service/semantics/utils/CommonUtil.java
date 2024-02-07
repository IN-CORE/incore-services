package edu.illinois.ncsa.incore.service.semantics.utils;

import edu.illinois.ncsa.incore.service.semantics.model.Column;
import edu.illinois.ncsa.incore.service.semantics.model.Type;
import java.util.Comparator;

public class CommonUtil {
    public static Comparator<Type> typeComparator(String sortBy, String order){
        // construct comparator
        Comparator<Type> comparator;
        if (sortBy.equals("name")) {
            comparator = Comparator.comparing(Type::getTitle);
        }
        else{
            // default to id
            comparator = Comparator.comparing(Type::getId);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }

    public static Comparator<Column> columnComparator(String sortBy, String order){
        // construct comparator
        Comparator<Column> comparator;
        if (sortBy.equals("title")) {
            comparator = Comparator.comparing(Column::getTitles);
        }
        else{
            // default to name
            comparator = Comparator.comparing(Column::getName);
        }
        if (order.equals("desc")) comparator = comparator.reversed();
        return comparator;
    }
}
