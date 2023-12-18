/*******************************************************************************
 * Copyright (c) 2023 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang
 */

package edu.illinois.ncsa.incore.service.dfr3.models;

import dev.morphia.annotations.Entity;

@Entity("Retrofit")
public class Retrofit {
    private String name;
    private String description;
    private PaperReference paperReference;
    private String unit;
    private String type; // enum, number, string, boolean
    private String[] allowedValues;
    private String[] allowedNames;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public PaperReference getPaperReference(){
        return paperReference;
    }

    public void setPaperReference(PaperReference paperReference){
        this.paperReference = paperReference;
    }

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

    public String[] getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String[] getAllowedNames() {
        return allowedNames;
    }

    public void setAllowedNames(String[] allowedNames) {
        this.allowedNames = allowedNames;
    }
}
