package edu.illinois.ncsa.incore.service.semantic.model;

import org.mongodb.morphia.annotations.Embedded;
import java.util.List;

@Embedded
public class Columns {
    private List<Column> columns;

    public Columns() { }

    public Columns(List<Column> columns){
        this.columns = columns;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
