/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.models.mvz;

/**
 * Created by ywkim on 8/2/2017.
 */
public class ColumnMetadata {
    private String friendlyName;
    private int fieldLength;
    private String unit;
    private String columnId;
    private int sigFigs;
    private String unitType;
    private boolean isNumeric;
    private boolean isResult;

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
