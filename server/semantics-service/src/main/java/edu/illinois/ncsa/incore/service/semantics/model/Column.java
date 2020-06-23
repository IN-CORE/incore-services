package edu.illinois.ncsa.incore.service.semantics.model;

import dev.morphia.annotations.Property;
import dev.morphia.annotations.Embedded;

@Embedded
public class Column {

    private String name;
    private String titles;
    private String dataType;

    @Property("incore:agg-type")
    private String aggType;

    @Property("incore:field-length")
    private Integer fieldLength;

    @Property("incore:importance")
    private String importance;

    @Property("incore:is-numeric")
    private String isNumeric;

    @Property("incore:is-result")
    private String isResult;

    @Property("incore:unit")
    private String unit;

    public Column() { }

    public Column(String name, String titles, String dataType, String aggType, Integer fieldLength,
                  String importance, String isNumeric, String isResult, String unit) {
        this.name = name;
        this.titles = titles;
        this.dataType = dataType;
        this.aggType = aggType;
        this.fieldLength = fieldLength;
        this.importance = importance;
        this.isNumeric = isNumeric;
        this.isResult = isResult;
        this.unit = unit;
    }

    public String getName(){
        return this.name;
    }

    public String getTitles(){
        return this.titles;
    }

    public String getDataType(){
        return this.dataType;
    }

    public String getAggType() {
        return aggType;
    }

    public Integer getFieldLength() {
        return fieldLength;
    }

    public String getImportance() {
        return importance;
    }

    public String getIsNumeric() {
        return isNumeric;
    }

    public String getIsResult() {
        return isResult;
    }

    public String getUnit() {
        return unit;
    }
}
