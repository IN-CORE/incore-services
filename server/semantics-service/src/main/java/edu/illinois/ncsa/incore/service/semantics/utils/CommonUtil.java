package edu.illinois.ncsa.incore.service.semantics.utils;

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
}
