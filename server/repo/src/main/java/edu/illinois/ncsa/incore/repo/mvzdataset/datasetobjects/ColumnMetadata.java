package edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects;

/**
 * Created by ywkim on 8/2/2017.
 */
public class ColumnMetadata {
    public String friendlyName;
    public int fieldLength;
    public String unit;
    public String columnId;
    public int sigFigs;
    public String unitType;
    public boolean isNumeric;
    public boolean isResult;

    public String getFriendlyName(){
        return friendlyName;
    }
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public int getFieldLength() {
        return fieldLength;
    }
    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getUnit(){
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getColumnId() {
        return columnId;
    }
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public int getSigFigs() {
        return sigFigs;
    }
    public void setSigFigs(int sigFigs) {
        this.sigFigs = sigFigs;
    }

    public String getUnitType() {
        return unitType;
    }
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public boolean getIsNumeric() {
        return isNumeric;
    }
    public void setIsNumeric(boolean isNumeric){
        this.isNumeric = isNumeric;
    }

    public boolean getIsResult() {
        return isResult;
    }
    public void setIsResult(boolean isResult){
        this.isResult = isResult;
    }
}
