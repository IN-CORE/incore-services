package edu.illinois.ncsa.incore.services.fragility.mapping;

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
