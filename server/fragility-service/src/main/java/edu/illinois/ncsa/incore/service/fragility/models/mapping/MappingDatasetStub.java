/*
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.fragility.models.mapping;

import ncsa.tools.common.UserFacing;
import org.dom4j.Element;

/**
 * Not interested in pulling in the whole MappingDataset from Ergo v1 right now, but adding this
 * so that it can deserialize the fragility mapping datasets from v1 repos
 */
public class MappingDatasetStub implements UserFacing {

    public MatchFilterMap getMatchFilterMap() {
        return mfm;
    }

    MatchFilterMap mfm = new MatchFilterMap();

    @Override
    public void initializeFromElement(Element element) {
        mfm.initializeFromElement(element.element("match-filter-map"));
    }

    @Override
    public Element asElement() {
        //TODO: add this later so we can reserialize
        return null;
    }
}
