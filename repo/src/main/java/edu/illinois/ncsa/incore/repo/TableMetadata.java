package edu.illinois.ncsa.incore.repo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 8/2/2017.
 */
public class TableMetadata {
    public List<ColumnMetadata> columnMetadata = new LinkedList<ColumnMetadata>();

    public List<ColumnMetadata> getColumnMetadata() {
        return columnMetadata;
    }
    public void setColumnMetadata(List<ColumnMetadata> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}
