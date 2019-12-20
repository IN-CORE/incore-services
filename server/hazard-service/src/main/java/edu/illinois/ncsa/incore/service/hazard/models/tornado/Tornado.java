/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tornadoType")
@JsonSubTypes({@JsonSubTypes.Type(value = TornadoDataset.class, name = "dataset"), @JsonSubTypes.Type(value = TornadoModel.class, name = "model")})
@XmlRootElement
public class Tornado {
    @Id
    @Property("_id")
    private ObjectId id;
    // Friendly name of Tornado hazard
    private String name;
    private String description;
    private String creator = null;

    private Date date = new Date();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id.toString();
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

    public void setCreator(String creator) { this.creator = creator; }

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
}
