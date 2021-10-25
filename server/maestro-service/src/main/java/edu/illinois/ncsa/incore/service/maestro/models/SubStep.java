/*******************************************************************************
 * Copyright (c) 2021 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chen Wang (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.maestro.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.morphia.annotations.Embedded;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

@XmlTransient
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "className")
@XmlSeeAlso({TeamSubStep.class, SituationSubStep.class, ObjectiveSubStep.class, PlanDevSubStep.class, PlanRevSubStep.class,
    PlanImpSubStep.class,})
@Embedded
public class SubStep {
    public String id;
    public String name;
    public String description;
    public Status status;
    public Boolean required;
    public List<String> requiredSteps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public List<String> getRequiredSteps() {
        return requiredSteps;
    }

    public void setRequiredSteps(List<String> requiredSteps) {
        this.requiredSteps = requiredSteps;
    }
}
