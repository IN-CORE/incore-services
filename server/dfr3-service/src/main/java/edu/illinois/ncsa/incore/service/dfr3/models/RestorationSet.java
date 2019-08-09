/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.dfr3.models;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@Entity("RestorationSet")
public class RestorationSet {
    @Id
    private ObjectId id;

    @XmlTransient
    private String legacyId;

    private String description;

    private List<String> authors = new ArrayList<>();
    private PaperReference paperReference;

    private String resultUnit;
    private String resultType;
    private String timeUnits;
    private String hazardType;
    private String inventoryType;

    private List<RestorationCurve> restorationCurves;
    private Privileges privileges;
    private String creator;

    public RestorationSet() {
    }

    public RestorationSet(String legacyId, String description, List<String> authors, PaperReference paperReference, String resultUnit,
                          String resultType, String timeUnits, String hazardType, String inventoryType,
                          List<RestorationCurve> restorationCurves) {
        this.legacyId = legacyId;
        this.description = description;
        this.authors = authors;
        this.paperReference = paperReference;
        this.resultUnit = resultUnit;
        this.resultType = resultType;
        this.timeUnits = timeUnits;
        this.hazardType = hazardType;
        this.inventoryType = inventoryType;
        this.restorationCurves = restorationCurves;
    }

    // region Getters
    public String getId() {
        if (id == null) {
            return null;
        } else {
            return id.toHexString();
        }
    }

    public String getLegacyId() {
        return legacyId;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public PaperReference getPaperReference() {
        return paperReference;
    }

    public String getResultUnit() {
        return resultUnit;
    }

    public String getResultType() {
        return resultType;
    }

    public String getTimeUnits() {
        return timeUnits;
    }

    public String getHazardType() {
        return hazardType;
    }

    public String getInventoryType() {
        return inventoryType;
    }

    public List<RestorationCurve> getRestorationCurves() {
        return restorationCurves;
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
    // endregion
}
