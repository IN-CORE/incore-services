/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.utils.TornadoCalc;
import org.bson.json.JsonParseException;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tornadoType")
@JsonSubTypes({@JsonSubTypes.Type(value = TornadoDataset.class, name = "dataset"), @JsonSubTypes.Type(value = TornadoModel.class, name =
    "model")})
@XmlRootElement
public class Tornado {
    @Id
    @Property("_id")
    private ObjectId id;
    // Friendly name of Tornado hazard
    private String name;
    private String description;
    private String creator = null;
    private String owner = null;
    private Double threshold = null;
    private String thresholdUnit = TornadoHazard.WIND_MPH;

    /**
     * spaces the object belongs to. Calculated at runtime.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    private Date date = new Date();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    /**
     * Date created
     *
     * @return date created
     */
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(String thresholdUnit) {
        thresholdUnit = thresholdUnit.trim();
        if (thresholdUnit.equalsIgnoreCase(TornadoHazard.WIND_MPH) || thresholdUnit.equalsIgnoreCase(TornadoHazard.WIND_MPS)){
            this.thresholdUnit = thresholdUnit;
        }
        else {
            throw new JsonParseException("Invalid thresholdUnits");
        }
    }

    @JsonIgnore
    public String getThresholdJsonString(){
        // Always converts the threshold value to mph. This is a workaround because internally all tornado calculations happen in mph.
        String retStr = String.format("{'wind': {'value': %s, 'unit': 'mph'}}", this.threshold);

        if (this.thresholdUnit.equalsIgnoreCase(TornadoHazard.WIND_MPS)  && this.threshold != null){
            retStr = String.format("{'wind': {'value': %s, 'unit': 'mph'}}",
                this.threshold/TornadoCalc.getConversionFactor(TornadoHazard.WIND_MPS));
        }
        return retStr;
    }
}
