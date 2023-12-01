package edu.illinois.ncsa.incore.service.semantics.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.semantics.model.Type;
import jakarta.ws.rs.core.Response;
import org.json.simple.JSONObject;

import java.util.Comparator;
import java.util.List;

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
