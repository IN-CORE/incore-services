package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Embedded;
import java.util.List;
import java.util.stream.Collectors;
import static edu.illinois.ncsa.incore.service.semantics.utils.CommonUtil.columnComparator;

@Embedded()
public class Columns {
    private List<Column> columns;

    public Columns() {
    }

    public Columns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Column> getSortedColumns(String sortBy, String order) {
        return this.columns.stream()
            .sorted(columnComparator(sortBy, order))
            .collect(Collectors.toList());
    }
}
