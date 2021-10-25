/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Entity("Playbook")
public class Playbook {
    @Id
    @Property("_id")
    protected ObjectId id;

    public String name;
    public String description;
    public List<String> tags;
    public List<Step> steps;

    // region Getters
    public String getId() {
        return (id == null) ? null : id.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Step> getSteps() {
        return steps;
    }
}
