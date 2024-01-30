package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class MappingEntryKeyConfig {
    private String unit;
    private String type;
    private String targetColumn;
    private String expression;

    public String getUnit(){
        return unit;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getTargetColumn(){
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn){
        this.targetColumn = targetColumn;
    }

    public String getExpression(){
        return expression;
    }

    public void setExpression(String expression){
        this.expression = expression;
    }
}
