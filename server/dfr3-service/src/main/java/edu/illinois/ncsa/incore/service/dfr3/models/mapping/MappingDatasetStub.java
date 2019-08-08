/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Omar Elabd, Nathan Tolbert
 */

package edu.illinois.ncsa.incore.service.dfr3.models.mapping;

import ncsa.tools.common.UserFacing;
import org.dom4j.Element;

/**
 * Not interested in pulling in the whole MappingDataset from Ergo v1 right now, but adding this
 * so that it can deserialize the dfr3 mapping datasets from v1 repos
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
