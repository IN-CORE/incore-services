package edu.illinois.ncsa.incore.service.hazard.utils;

import edu.illinois.ncsa.incore.common.exceptions.IncoreHTTPException;
import edu.illinois.ncsa.incore.service.hazard.models.eq.types.IncorePoint;

import javax.ws.rs.core.Response;
import java.util.List;

public class CommonUtil {
    public static void validateHazardValuesInput(List<String> demands, List<String> units,
                                                 IncorePoint point){
        if(demands == null || units == null || point == null){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST,  "Please check if demands, units and location" +
                " are provided for every element in the request json");
        }

        if(demands.size() == 0 || units.size() == 0 || demands.size() != units.size()){
            throw new IncoreHTTPException(Response.Status.BAD_REQUEST, "The demands and units for at least one of " +
                "the locations are either missing or not of the same size");
        }

    }
}
