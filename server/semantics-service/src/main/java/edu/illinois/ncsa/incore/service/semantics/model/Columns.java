package edu.illinois.ncsa.incore.service.semantics.model;

import java.util.List;

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
