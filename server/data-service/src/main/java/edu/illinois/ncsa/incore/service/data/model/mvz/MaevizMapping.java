package edu.illinois.ncsa.incore.service.data.model.mvz;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 8/2/2017.
 */
public class MaevizMapping {
    private String schema;
    private List<Mapping> mapping = new LinkedList<Mapping>();

    public String getSchema() {
        return schema;
    }
    public void setSchema(String schema){
        this.schema = schema;
    }

    public List<Mapping> getMapping() { return mapping; }
    public void setMapping(List<Mapping> mapping) {
        this.mapping = mapping;
    }

}
